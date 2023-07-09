package uk.ac.ed.inf;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import com.google.gson.reflect.TypeToken;
import com.google.gson.Gson;

/**
 * This class is used to handle data about shops and their items.
 */
public class Menus {
    /**
     * in this project, it is localhost
     */
    private String machineName;
    /**
     * the website port
     */
    private String port;

    /**
     * constructor of menus
     * @param nameOfMachine in this project, it is localhost
     * @param port the website port
     */
    public Menus(String nameOfMachine, String port) {
        this.machineName = nameOfMachine;
        this.port = port;
    }

    /**
     * it used to get data from website
     */
    public class item {
        String item;
        Integer pence;
    }

    /**
     * it used to get data from website
     */
    public class shop {
        Type listType = new TypeToken<ArrayList<item>>() {
        }.getType();
        String name;
        String location;
        ArrayList<item> menu;
    }

    /**
     * Use these input items to calculate the total cost of this delivery,
     * calculate total cost of all the input items and add 50 pence for each delivery
     *
     * @param itemName list of items in this delivery
     * @return return total delivery cost or print error message and return null.
     */
    public static Double getDeliveryCostOfItemArray(ArrayList<String> itemName) {
        Double cost = 0.0;//initialise the total cost
        URI uri = URI.create("http://" + "localhost" + ":" + Drone.webPort + "/menus/menus.json");//initialize the uri with input webPort.
        HttpRequest request = HttpUtils.setRequest(uri);
        try {
            HttpResponse<String> response = HttpUtils.getResponse(request);//get response from webserver
            if (response.statusCode() == 404) {//handle 404 error
                System.err.println("404 not found");
                return null;
            } else {
                if (response.statusCode() == 200) {//handle normally returned data
                    String jsonString = response.body();
                    Type listType = new TypeToken<ArrayList<shop>>() {}.getType();
                    ArrayList<shop> shopList = new Gson().fromJson(jsonString, listType);
                    for (String i : itemName) {//loop through all items in menus, find items we want and add its cost to total.
                        for (shop j : shopList) {
                            for (item k : j.menu) {
                                if (k.item.equals(i)) {
                                    cost += k.pence;
                                }
                            }
                        }
                    }
                    cost += 50;//add the delivery charge
                    return cost;
                } else {
                    System.err.println("unknown error");
                    return null;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Use a list of items to get the list of 3 word address of related shops
     * @param items the input list of items used to search related shops
     * @return return a list of string of 3word address of related shops
     */
    public static List<String> getShops3WordLocationByItems(ArrayList<String> items) {
        List<String> shopLoc = new ArrayList<String>();//initialize the list of 3word address
        URI uri = URI.create("http://" + "localhost" + ":" + Drone.webPort + "/menus/menus.json");//initialize the uri with input webPort.
        HttpRequest request = HttpUtils.setRequest(uri);
        try {
            HttpResponse<String> response = HttpUtils.getResponse(request);
            if (response.statusCode() == 404) {//handle 404 error
                System.err.println("404 not found");
                return null;
            } else {
                if (response.statusCode() == 200) {//handle normally returned data
                    String jsonString = response.body();
                    Type listType = new TypeToken<ArrayList<shop>>() {
                    }.getType();
                    ArrayList<shop> shopList = new Gson().fromJson(jsonString, listType);
                    for (String i : items) {//loop through all items in menus, find items we are looking for.
                        for (shop j : shopList) {
                            for (item k : j.menu) {
                                if (k.item.equals(i)) {
                                    if (shopLoc.contains(j.location)==false) {//check whether we have added this shop's location before
                                    shopLoc.add(j.location);//if we haven't add it, add this shop's location to the list of 3word address.
                                    }
                                }
                            }
                        }
                    }
                    return shopLoc;
                }
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}