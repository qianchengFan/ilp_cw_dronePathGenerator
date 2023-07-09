package uk.ac.ed.inf;


import com.google.gson.Gson;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

/**
 * The implementation of order class with its methods.
 */
public class Orders {
    /**
     * The order number
     */
    public String orderNo;
    /**
     * The deliver address
     */
    public String deliverTo;
    /**
     * The list of deliver items
     */
    public ArrayList<String> items;
    /**
     * total monetary value of this order
     */
    public Integer cost;

    /**
     * constructor of orders
     * @param orderNo the order number
     * @param deliverTo the deliver address
     * @param items the list of items
     */
    public Orders(String orderNo, String deliverTo, ArrayList<String> items) {
        this.orderNo = orderNo;
        this.deliverTo = deliverTo;
        this.items = items;
        this.cost = 0;
    }

    /**
     * it used for orders table in database
     */
    public class threeWordsCoordinates {
        double lng;
        double lat;
    }
    /**
     * it used for orders table in database
     */
    public class Square {
        threeWordsCoordinates southwest;
        threeWordsCoordinates northeast;
    }
    /**
     * it used for orders table in database
     */
    public class threeWords {
        String country;
        Square square;
        String nearestPlace;
        threeWordsCoordinates coordinates;
        String words;
        String languages;
        String map;
    }

    /**
     * convert a 3word address to a Longlat
     * @param word The 3word address that we want to convert
     * @return return the converted Longlat
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public static LongLat transformThreeWordToLongLat(String word) throws IOException, InterruptedException {
        String delimiters = "\\.";
        String[] words = word.split(delimiters);//split the 3word address to a list of 3 independent word.
        URI uri = URI.create("http://localhost" + ":" + Drone.webPort + "/words/" + words[0] + "/" + words[1] + "/" + words[2] + "/details.json");
        HttpRequest request = HttpUtils.setRequest(uri);
        try {
            HttpResponse<String> response = HttpUtils.getResponse(request);//get response from webserver
            if (response.statusCode() == 404) {//handle 404 error
                System.err.println("404 not found");
                return null;
            } else {
                if (response.statusCode() == 200) {//handle normally returned data
                    String jsonString = response.body();
                    threeWords threeWordsInfo = new Gson().fromJson(jsonString, threeWords.class);
                    double lng = threeWordsInfo.coordinates.lng;
                    double lat = threeWordsInfo.coordinates.lat;
                    return new LongLat(lng, lat);
                }
                else {
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
        }
    }
}
