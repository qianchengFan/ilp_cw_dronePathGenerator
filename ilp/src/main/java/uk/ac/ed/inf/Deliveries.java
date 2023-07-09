package uk.ac.ed.inf;

/**
 * This class is used for deliveries table in database.
 */
public class Deliveries {
    /**
     * The order number
     */
    public String orderNo;
    /**
     * The deliver address
     */
    public String deliveredTo;
    /**
     * The monetary value of this order
     */
    public int costInPence;

    /**
     * The constructor of deliveries class
     * @param orderNo The order number
     * @param deliveredTo The deliver address
     * @param costInPence The monetary value of this order
     */
    public Deliveries(String orderNo, String deliveredTo, int costInPence) {
        this.orderNo = orderNo;
        this.deliveredTo = deliveredTo;
        this.costInPence = costInPence;
    }
}