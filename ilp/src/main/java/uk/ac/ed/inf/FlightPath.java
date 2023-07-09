package uk.ac.ed.inf;

/**
 * This class is used for flight path table in database.
 */
public class FlightPath {
    /**
     * The order number relate to this flight
     */
    public String orderNo;
    /**
     * longitude of start position
     */
    public double fromLongitude;
    /**
     * latitude of start position
     */
    public double fromLatitude;
    /**
     * angle of this flight
     */
    public int angle;
    /**
     * longitude of end position
     */
    public double toLongitude;
    /**
     * latitude of end position
     */
    public double toLatitude;

    /**
     * The constructor of Flight path
     * @param orderNo The order number of the order that relate to this flight
     * @param fromLongitude The longitude of the position that drone move from
     * @param fromLatitude The latitude of the position that drone move from
     * @param angle The move angle
     * @param toLongitude The longitude of the position that drone move to
     * @param toLatitude The latitude of the position that drone move to
     */
    public FlightPath(String orderNo, double fromLongitude, double fromLatitude, int angle, double toLongitude, double toLatitude) {
        this.orderNo = orderNo;
        this.fromLongitude = fromLongitude;
        this.fromLatitude = fromLatitude;
        this.angle = angle;
        this.toLongitude = toLongitude;
        this.toLatitude = toLatitude;
    }
}
