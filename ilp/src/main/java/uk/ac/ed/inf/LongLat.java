package uk.ac.ed.inf;
import com.mapbox.geojson.Point;

import java.lang.Math;

/**
 * The implementation of LongLat class with its methods.
 */
public class LongLat {
    /**
     * longitude of longlat
     */
    public double longitude;
    /**
     * latitude of longlat
     */
    public double latitude;

    /**
     * constructor of longlat
     * @param longitude longitude of longlat
     * @param latitude latitude of longlat
     */
    public LongLat(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    /**
     *
     * check the confinement area
     *
     * @return the boolean that whether the confinement area is confined.
     */
    public boolean isConfined(){
        if (longitude<-3.192473){return false;}
        if (longitude>-3.184319){return false;}
        if (latitude >55.946233){return false;}
        if (latitude <55.942617){return false;}
        return true;
    }

    /**
     *
     * calculate the distance between the local position and the input position p2
     *
     * @param p2 The input position with data type longLat
     * @return the distance between local position and the input position p2
     */
    public double distanceTo(LongLat p2){
        return (Math.sqrt(Math.pow((this.longitude-p2.longitude),2)+Math.pow((this.latitude-p2.latitude),2)));
    }

    /**
     *
     * To check if the distance between the local position and the input position p2 is smaller than 0.00015 degrees
     *
     * @param p2 The input position with data type longLat
     * @return a boolean value that whether distance between these two position is smaller than 0.00015 degrees
     */
    public boolean closeTo(LongLat p2){
        if (distanceTo(p2)<0.00015){//check whether the distance smaller than 0.00015 degrees
            return true;
        }
        else {
            return false;
        }
    }

    /**
     *
     * calculate the new position of the drone if it makes a move in the direction of the input angle
     *
     * @param direction is the input angle of the direction of this move.
     * @return the position with data type longLat after making a move by the input direction, or return error
     * message if the input angle is not valid. Or stay the same position if the input angle is -999.
     */
    public LongLat nextPosition(int direction){
        if (direction ==-999){return new LongLat(longitude,latitude);}
        if (direction %10 !=0){throw new IllegalArgumentException("direction must be a multiple of ten degrees");}
        if (direction < 0 || direction > 350){throw new IllegalArgumentException("direction must in range 0~350");}
        double newLongitude =longitude + 0.00015 * Math.cos(Math.toRadians(direction));//use the direction angle to calculate the location of new position.
        double newLatitude =latitude + 0.00015 * Math.sin(Math.toRadians(direction));
        return new LongLat(newLongitude,newLatitude);
    }

    /**
     * @return the latitude of this longlat
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * @return the longitude of this longlat
     */
    public double getLongitude() {
        return longitude;
    }
}
