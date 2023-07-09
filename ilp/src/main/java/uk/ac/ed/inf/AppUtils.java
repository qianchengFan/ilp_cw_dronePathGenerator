package uk.ac.ed.inf;

import com.mapbox.geojson.*;
import java.util.ArrayList;

/**
 * This class contains app's helper function.
 */
public class AppUtils {
    /**
     * convert a list of point to a geojson string
     * @param points a list of point
     * @return a geojson string
     */
    public static String transformPointsToJson(ArrayList<Point> points) {
        ArrayList<Feature> featureList = new ArrayList<Feature>();
        LineString ls = LineString.fromLngLats(points);
        Feature f = Feature.fromGeometry(ls);
        featureList.add(f);
        FeatureCollection fc = FeatureCollection.fromFeatures(featureList);
        return fc.toJson();
    }
}
