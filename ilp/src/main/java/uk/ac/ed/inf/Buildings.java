package uk.ac.ed.inf;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.Point;
import java.awt.geom.Point2D;
import java.awt.geom.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * it's the implementation of building class, a building is used to store data of a no-fly-zone building.
 */
public class Buildings {
    /**
     * The name of the building
     */
    private String buildingName;
    /**
     * the polygon of the building
     */
    private Polygon coordinates;

    /**
     * The constructor of building class
     * @param name The name of the building
     * @param coordinates the polygon of the building
     */
    public Buildings(String name, Polygon coordinates) {
        this.buildingName = name;
        this.coordinates = coordinates;
    }

    /**
     * @return name of this building
     */
    public String getName() {
        return buildingName;
    }

    /**
     * @return polygon of this building
     */
    public Polygon getCoordinates() {
        return coordinates;
    }

    /**
     * get data of no-fly-zone from database and return them as a list of Buildings.
     *
     * @return a list of buildings.
     */
    public static ArrayList<Buildings> getBuildings() {
        //initialize the uri with input webPort.
        URI uri = URI.create("http://" + "localhost" + ":" + Drone.webPort + "/buildings/no-fly-zones.geojson");
        //initialize the request.
        HttpRequest request = HttpUtils.setRequest(uri);
        //initialize an empty building list.
        ArrayList<Buildings> buildings = new ArrayList<Buildings>();
        try {
            HttpResponse<String> response = HttpUtils.getResponse(request);//get response from webserver
            if (response.statusCode() == 404) {
                System.err.println("404 not found");//handle 404 error
                return null;
            } else {
                if (response.statusCode() == 200) {//handle normally returned data
                    String jsonString = response.body();
                    FeatureCollection fc = FeatureCollection.fromJson(jsonString);//get feature collection from returned geojson String.
                    for (int i = 0; i < fc.features().size(); i++) {//loop through each feature, store each feature as a building and add to buildings list
                        Feature f = fc.features().get(i);
                        String name = f.properties().get("name").getAsString();
                        Geometry g = f.geometry();
                        Polygon polygon = (Polygon) g;
                        Buildings bTemp = new Buildings(name, polygon);
                        buildings.add(bTemp);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return buildings;
    }

    /**
     * check whether the input line have intersection with input building. This method will
     * check the line with each edge of the polygon(the building), make sure the line will
     * not intersect with any edge of the building.
     *
     * @param b the no-fly-zone building.
     * @param line the line that need to check whether intersect with no-fly-zone building.
     * @return if no intersect, return true, else, return false.
     */
    public static boolean noIntersection(Buildings b, Line2D line) {
        boolean flag = true;
        List<Point> pl = b.coordinates.coordinates().get(0);//convert polygon to a list of points.
        for (int i = 0; i < pl.size() -1 ; i++) {//check there is no intersection on any edge of polygon by for loop.
            double lon1 = pl.get(i).longitude();
            double lat1 = pl.get(i).latitude();
            double lon2 = pl.get(i+1).longitude();
            double lat2 = pl.get(i+1).latitude();
            Point2D p1 = new Point2D.Double(lon1,lat1);
            Point2D p2 = new Point2D.Double(lon2,lat2);
            Line2D line2 = new Line2D.Double(p1,p2);

            if(line.intersectsLine(line2) || line2.intersectsLine(line)) {//if the line intersect with any edge, return value will be false.
                flag = false;
            }
        }
        return flag;
    }

}
