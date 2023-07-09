package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.awt.geom.*;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of drone class with its methods.
 */
public class Drone {
    /**
     * The current position of drone
     */
    private LongLat currentPosition;
    /**
     * The list of point that the drone has visited
     */
    private ArrayList<Point> visitedPoint;
    /**
     * The number of action left that the drone can do
     */
    private int actionLeft;
    /**
     * The total number of orders that the drone has delivered
     */
    private int deliveriedTotal;
    /**
     * This field used to store the current order the drone carries
     */
    private Orders tempOrder;
    /**
     * The initial position, which is the appleton tower
     */
    public static final LongLat initialPosition = new LongLat(-3.186874,55.944494);
    /**
     * The list of from position of each move that stored for flight path table
     */
    private ArrayList<Point> fromPosition;
    /**
     * The list of to position of each move that stored for flight path table
     */
    private ArrayList<Point> toPosition;
    /**
     * The list of delivered order number that stored for flight path table
     */
    private ArrayList<String> DeliverdorderNo;
    /**
     * The list of angle for each move that stored for flight path table
     */
    private ArrayList<Integer> angleUsed;
    /**
     * The list of order that stored for deliveries table
     */
    private ArrayList<Orders> ordersDeliverd;
    /**
     * The website port
     */
    public static String webPort;
    /**
     * The database port
     */
    public static String databasePort;
    /**
     * The date we get orders from
     */
    public static String date;

    /**
     * drone's constructor
     * @param webPort the webPort number
     * @param dataPort the database port number
     * @param date the date we input
     */
    public Drone(String webPort,String dataPort,String date){
        this.currentPosition = initialPosition;
        this.visitedPoint = new ArrayList<Point>();
        visitedPoint.add(Point.fromLngLat(initialPosition.longitude,initialPosition.latitude));
        this.tempOrder=null;
        this.actionLeft = 1500;
        this.deliveriedTotal = 0;
        this.DeliverdorderNo = new ArrayList<String>();
        this.angleUsed = new ArrayList<Integer>();
        this.ordersDeliverd = new ArrayList<Orders>();
        this.fromPosition = new ArrayList<Point>();
        this.toPosition = new ArrayList<Point>();
        this.webPort = webPort;
        this.databasePort = dataPort;
        this.date = date;
    }

    /**
     * @return the number of moves that the drone can still move
     */
    public Integer getactionLeft() {return  actionLeft;}

    /**
     * @return the database port
     */
    public String getDatabasePort() {
        return databasePort;
    }

    /**
     * @return the web port
     */
    public String getWebPort() {
        return webPort;
    }

    /**
     * @return the drone's current position.
     */
    public LongLat getCurrentPosition() {
        return currentPosition;
    }

    /**
     * @return the list of visited point
     */
    public ArrayList<Point> getVisitedPoint() {
        return visitedPoint;
    }

    /**
     * @return the list of each move's move angle.
     */
    public ArrayList<Integer> getAngleUsed() {
        return angleUsed;
    }

    /**
     * @return a list of delivered order
     */
    public ArrayList<Orders> getOrdersDeliverd() {
        return ordersDeliverd;
    }

    /**
     * @return a list of delivered order number
     */
    public ArrayList<String> getDeliverdorderNo() {
        return DeliverdorderNo;
    }

    /**
     * @return total number of delivered orders
     */
    public int getDeliveriedTotal() {
        return deliveriedTotal;
    }

    /**
     * generate flight path between 2 points by greedy move, for each move, I pick a valid angle that can make next position
     * closest to destination ,this may cause local optimal problem, if the flight have too many number of moves, it means it meet this problem,
     * then I will stop it and the returned flight will contain a signal 999 to show this solution is stuck, should try other generate method.
     * @param currentPosition current position of drone
     * @param destination the position that drone want to go
     * @param bL the list of no-fly-zone buildings
     * @return the generated flight path
     */
    private Flight generatePathBetween2PointsWithGreedyMova(LongLat currentPosition, LongLat destination, ArrayList<Buildings> bL){
        //Initialize variables that needed for flight path.
        List<Point> getFlightPath = new ArrayList<Point>();
        List<Point> from = new ArrayList<Point>();
        List<Point> to = new ArrayList<Point>();
        List<Integer> angles = new ArrayList<Integer>();
        Integer count = 0;//use count to count the number of loops
        LongLat dronePosition = currentPosition;
        Integer moves = 0;
        getFlightPath.add(Point.fromLngLat(dronePosition.longitude,dronePosition.latitude));
        //If the drone don't need to move, just return current position with signal -1
        if (dronePosition.distanceTo(destination)<0.00015){
            Flight flight = new Flight(moves, getFlightPath, from, to, angles);
            flight.tempIndex = -1;
            return flight;
        }
        //initialize a boolean to represent whether the method success with no local optimal problem.
        Boolean success = true;
        //use while loop generate moves until the drone arrive destination.
        while (dronePosition.distanceTo(destination)>=0.00015) {
            count +=1;
            //if the flight have more than 100 moves, it may meet local optimal problem, so I make it break out of loop
            //and set the boolean to false.
            if (count >100){
                success = false;
                break;
            }
            //add the current position to from list.
            from.add(Point.fromLngLat(dronePosition.longitude, dronePosition.latitude));
            //initialize local variables to record the point that closest to destination.
            double closest = 99999;
            LongLat closestNext = null;
            Integer angle = 0;
            //loop through all 36 possible angles, find the one that will not cause intersection and closest to destination.
            for (int i = 0; i < 36; i++) {
                LongLat next = dronePosition.nextPosition(i * 10);
                if (next.distanceTo(destination) < closest && next.isConfined() && noFlyZoneCheck(dronePosition, next, bL)) {
                    closest = next.distanceTo(destination);
                    closestNext = next;
                    angle = i * 10;
                }
            }
            //record this move.
            Point nextPoint = Point.fromLngLat(closestNext.longitude, closestNext.latitude);
            to.add(nextPoint);
            angles.add(angle);
            getFlightPath.add(nextPoint);
            dronePosition = closestNext;
            moves += 1;
        }
        //Use all the moves we get above generate a flight path.
        Flight flight = new Flight(moves, getFlightPath, from, to, angles);
        //if the boolean is false, it means we meet local optimal problem, so we set the signal to 999.
        if (success==false) {
            flight.tempIndex = 999;
        }
        return flight;
    }

    /**
     * This algorithm that can generate flight path between 2 points and will not have local optimal problem, but may have
     * more moves.
     * @param current current position of drone
     * @param destination the position that drone want to go
     * @param buildings the list of no-fly-zone buildings
     * @return the generated flight path
     */
    private Flight handleStuckFlight(LongLat current, LongLat destination, ArrayList<Buildings> buildings){
        //if there is no no-fly-zone building between this 2 points, just use the above method.
        if(noFlyZoneCheck(current,destination,buildings)){
            Flight directFlight = generatePathBetween2PointsWithGreedyMova(current,destination,buildings);
            return directFlight;
        }
        else{
            //Initialize variables that needed for flight path.
            LongLat dronePosition = current;
            List<Point> getFlightPath = new ArrayList<Point>();
            List<Point> from = new ArrayList<Point>();
            List<Point> to = new ArrayList<Point>();
            List<Integer> angles = new ArrayList<Integer>();
            Integer moves = 0;
            getFlightPath.add(Point.fromLngLat(current.longitude, current.latitude));
            //use while loop generate moves until the drone arrive destination.
            while (dronePosition.distanceTo(destination)>=0.00015) {
                from.add(Point.fromLngLat(dronePosition.longitude, dronePosition.latitude));
                //initialize local variables to record the point that closest to destination.
                double closest = 99999;
                Integer angle = 0;
                //check whether the current position can directly move to destination
                if (noFlyZoneCheck(dronePosition, destination, buildings) == false) {
                    LongLat next;
                    //first find the angle that make the point as close as possible to destination.
                    for (int i = 0; i < 36; i++) {
                        next = dronePosition.nextPosition(i * 10);
                        if (next.distanceTo(destination) < closest && next.isConfined()) {
                            closest = next.distanceTo(destination);
                            angle = i * 10;
                        }
                    }
                    //use fifteen move check to find the angle we want, and calculate the next position.
                    next = dronePosition.nextPosition(fifteenMoveCheck(dronePosition, angle, buildings));
                    //record this move.
                    Point nextPoint = Point.fromLngLat(next.longitude, next.latitude);
                    to.add(nextPoint);
                    angles.add(angle);
                    getFlightPath.add(nextPoint);
                    dronePosition = next;
                    moves += 1;
                //if the current position can directly move to destination, just use the above method do rest moves
                    //and generate flight path after record these moves.
                } else {
                    Flight followingFlight = generatePathBetween2PointsWithGreedyMova(dronePosition,destination,buildings);
                    to.add(followingFlight.to.get(0));
                    angles.add(followingFlight.angle.get(0));
                    getFlightPath.add(followingFlight.path.get(0));
                    moves +=1;
                    if(followingFlight.from.size()>1) {//prevent index out of range
                        for (int i = 1; i < followingFlight.from.size(); i++) {
                            from.add(followingFlight.from.get(i));
                            to.add(followingFlight.to.get(i));
                            angles.add(followingFlight.angle.get(i));
                            getFlightPath.add(followingFlight.path.get(i));
                            moves += 1;
                        }
                        getFlightPath.add(followingFlight.path.get(followingFlight.path.size()-1));
                    }
                    Flight flight = new Flight(moves,getFlightPath,from,to,angles);
                    return flight;
                }
            }
            //return flight path.
            Flight flight = new Flight(moves,getFlightPath,from,to,angles);
            return flight;
        }
    }

    /**
     * This method will find a angle that can make the lineString of current position and the position
     * after 15 moves with this angle not intersect with any no-fly-zone building, and close to the input angle.
     * @param current current position of drone
     * @param angle the input angle
     * @param buildings the list of no-fly-zone buildings
     * @return the generated angle.
     */
    private Integer fifteenMoveCheck(LongLat current, Integer angle, ArrayList<Buildings> buildings){
        //calculate the position after 15 moves with the input angle before enter the while loop.
        LongLat next = current.nextPosition(angle);
        LongLat next2 = next.nextPosition(angle);
        LongLat next3 = next2.nextPosition(angle);
        LongLat next4 = next3.nextPosition(angle);
        LongLat next5 = next4.nextPosition(angle);
        LongLat next6 = next5.nextPosition(angle);
        LongLat next7 = next6.nextPosition(angle);
        LongLat next8 = next7.nextPosition(angle);
        LongLat next9 = next8.nextPosition(angle);
        LongLat next0 = next9.nextPosition(angle);
        LongLat n1 = next0.nextPosition(angle);
        LongLat n2 = n1.nextPosition(angle);
        LongLat n3 = n2.nextPosition(angle);
        LongLat n4 = n3.nextPosition(angle);
        LongLat n5 = n4.nextPosition(angle);

        //use while loop, each loop -10 degree on the angle, until find the angle we want.
        while (noFlyZoneCheck(current, n5, buildings) == false) {
            //use method check360 to keep the angle at 0 ~ 360.
            angle = check360(angle - 10);
            next = current.nextPosition(angle);
            next2 = next.nextPosition(angle);
            next3 = next2.nextPosition(angle);
            next4 = next3.nextPosition(angle);
            next5 = next4.nextPosition(angle);
            next6 = next5.nextPosition(angle);
            next7 = next6.nextPosition(angle);
            next8 = next7.nextPosition(angle);
            next9 = next8.nextPosition(angle);
            next0 = next9.nextPosition(angle);
            n1 = next0.nextPosition(angle);
            n2 = n1.nextPosition(angle);
            n3 = n2.nextPosition(angle);
            n4 = n3.nextPosition(angle);
            n5 = n4.nextPosition(angle);
        }
        return angle;
    }

    /**
     * Used to keep the angle at 0 ~ 360, for example, if the input angle is 370
     * we will return 10.
     * @param angle input angle
     * @return the angle at 0~360
     */
    private Integer check360(Integer angle){
            if (angle >= 360){
                angle -= 360;
            }
            if (angle < 0){
                angle += 360;
            }
        return angle;
    }

    /**
     * generate the flight from start position to appleton tower.
     * @param start the start position
     * @param bl the list of no-fly-zone buildings
     * @return the generated flight
     */
    private Flight backToTower(LongLat start, ArrayList<Buildings> bl){
        LongLat currentPosition = start;
        //generate flight path
        Flight check = generatePathBetween2PointsWithGreedyMova(currentPosition,initialPosition,bl);
        if (check.tempIndex==999){
            check = handleStuckFlight(currentPosition,initialPosition,bl);
        }
        return check;
    }

    /**
     * check the input line will not intersect with any no-fly-zone building
     * @param begin start position of the line
     * @param end end position of the line
     * @param bl a list of no-fly-zone building list
     * @return a boolean to represent whether the check success.
     */
    private static boolean noFlyZoneCheck(LongLat begin, LongLat end, ArrayList<Buildings> bl){
        boolean flag = true;
        //loop through all buildings to check not intersect with any of it.
        for (Buildings j : bl){
            Line2D.Double l = getLine2D(begin,end);
            if (Buildings.noIntersection(j,l)==false){
                flag = false;
            }
        }
        return flag;
    }

    /**
     * This method will produce flight path for an input order.
     * @param order the input order
     * @param bl the list of no-fly-zone buildings
     * @return te generated flight path
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private Flight orderFlight(Orders order,ArrayList<Buildings> bl) throws IOException, InterruptedException {
        //initialize required variables
        Flight orderFlight = new Flight(0,new ArrayList<Point>(),new ArrayList<Point>(),new ArrayList<Point>(),new ArrayList<Integer>());
        List<String> shops = Menus.getShops3WordLocationByItems(order.items);
        LongLat dronePosition = this.currentPosition;
        //the order may need to go to several shops, so use for loop to generate the flight of each part.
        for (int i = 0; i < shops.size(); i++) {
            LongLat location = Orders.transformThreeWordToLongLat(shops.get(i));
            Flight temp = generatePathBetween2PointsWithGreedyMova(dronePosition, location, bl);
            if (temp.tempIndex == 999){
                temp = handleStuckFlight(dronePosition, location, bl);
            }
            //if signal == -1, drone don't need to use move to go to next shop, so it stay there do next hover.
            if (temp.tempIndex == -1){
                orderFlight.moves +=1;//add hover
                orderFlight.from.add(temp.path.get(0));
                orderFlight.to.add(temp.path.get(0));
                orderFlight.angle.add(-999);
                orderFlight.path.add(temp.path.get(0));
            }
            //record information
            orderFlight.moves += temp.moves;
            for (int j = 0; j < temp.from.size(); j++) {
                orderFlight.from.add(temp.from.get(j));
                orderFlight.to.add(temp.to.get(j));
                orderFlight.angle.add(temp.angle.get(j));
                orderFlight.path.add(temp.path.get(j));
            }
            Integer lastIndex = temp.to.size()-1;
            if (lastIndex != -1){//prevent hover 2 times for same shop
                orderFlight.moves +=1;//add hover
                orderFlight.from.add(temp.to.get(lastIndex));
                orderFlight.to.add(temp.to.get(lastIndex));
                orderFlight.angle.add(-999);
                orderFlight.path.add(temp.path.get(lastIndex));
            }
            LongLat lastPoint = new LongLat(orderFlight.path.get(orderFlight.path.size()-1).longitude(),orderFlight.path.get(orderFlight.path.size()-1).latitude());
            dronePosition = lastPoint;//update current location
        }
        //then add the part that from last shop to delivery location.
        LongLat destination = Orders.transformThreeWordToLongLat(order.deliverTo);
        Flight temp1 = generatePathBetween2PointsWithGreedyMova(dronePosition, destination, bl);
        if (temp1.tempIndex == 999){
            temp1 = handleStuckFlight(dronePosition, destination, bl);
        }
        //record information
        orderFlight.moves += temp1.moves;
        for (int j = 0; j < temp1.from.size(); j++) {
            orderFlight.from.add(temp1.from.get(j));
            orderFlight.to.add(temp1.to.get(j));
            orderFlight.angle.add(temp1.angle.get(j));
            orderFlight.path.add(temp1.path.get(j));
        }
        Integer lastIndex = temp1.from.size()-1;
        if (lastIndex != -1){
            orderFlight.moves +=1;//add hover
            orderFlight.from.add(temp1.to.get(lastIndex));
            orderFlight.to.add(temp1.to.get(lastIndex));
            orderFlight.angle.add(-999);
            orderFlight.path.add(temp1.path.get(lastIndex));
        }
        return orderFlight;
    }

    /**
     * find the order that have highest ratio on monetary/moves and return the flight path for delivering the order.
     * @param orderList the input order list
     * @param bl the list of no-fly-zone buildings
     * @return the flight path for the most efficient order
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    private Flight findMostEfficientOrder(ArrayList<Orders> orderList,ArrayList<Buildings> bl) throws IOException, InterruptedException {
        //initialize required variables.
        Flight maxFlight = orderFlight(orderList.get(0),bl);
        ArrayList<String> itemList = orderList.get(0).items;
        Double cost = Menus.getDeliveryCostOfItemArray(itemList);
        Double maxRate = 0.0;
        Integer tempIndex = 0;//record the index of order in orderList

        //loop through the order list, find the most efficient order and record its information.
        for (int i = 0; i < orderList.size(); i++) {
            Orders order = orderList.get(i);
            Double tempCost = Menus.getDeliveryCostOfItemArray(order.items);
            Flight temp = orderFlight(orderList.get(i),bl);
            Double tempRate = tempCost/temp.moves;
            if (tempRate > maxRate){
                maxRate = tempRate;
                maxFlight = temp;
                tempIndex = i;
                this.tempOrder = order;
                tempOrder.cost = (int)Math.round(Menus.getDeliveryCostOfItemArray(tempOrder.items));
            }
        }
        //record the index of this order is used to remove this order from order list later in method generateGreedysolution.
        maxFlight.tempIndex=tempIndex;
        return maxFlight;
    }

    /**
     * This method use greedy algorithm to generate solution. Each time,we chose the order that itself have highest
     * monetary/moves ratio, and remove it from order list, we repeat this process until all orders delivered or
     * drone's battery only allow it used to return to appleton tower.
     * @param orderList the input order list
     * @param bl the list of no-fly-zone buildings
     * @throws IOException IOException
     * @throws InterruptedException InterruptedException
     */
    public void generateGreedysolution(ArrayList<Orders> orderList,ArrayList<Buildings> bl) throws IOException, InterruptedException {

        //use while loop go through all orders
        while(orderList.size()!=0){
            Flight flight = findMostEfficientOrder(orderList,bl);
            LongLat positionAfterThisOrder = new LongLat(flight.to.get(flight.to.size()-1).longitude(),flight.to.get(flight.to.size()-1).latitude());

            //check whether battery is enough to deliver this order and return to appleton tower.
            if (this.actionLeft >= (flight.moves + backToTower(positionAfterThisOrder,bl).moves)) {
                this.currentPosition = new LongLat(flight.to.get(flight.to.size() - 1).longitude(), flight.to.get(flight.to.size() - 1).latitude());
                this.actionLeft -= flight.moves;
                this.ordersDeliverd.add(tempOrder);
                this.deliveriedTotal+=1;
                for (int i = 0; i < flight.from.size(); i++) {
                    this.fromPosition.add(flight.from.get(i));
                    this.toPosition.add(flight.to.get(i));
                    this.angleUsed.add(flight.angle.get(i));
                    this.visitedPoint.add(flight.to.get(i));
                    this.DeliverdorderNo.add(tempOrder.orderNo);
                }
            }
            //remove the order from order list
            orderList.remove(orderList.get(flight.tempIndex));
        }
        //add the flight that drone return to appleton tower.
        Flight flightToTower = backToTower(this.currentPosition,bl);
        this.currentPosition = new LongLat(flightToTower.to.get(flightToTower.to.size() - 1).longitude(), flightToTower.to.get(flightToTower.to.size() - 1).latitude());
        this.actionLeft -= flightToTower.moves;
        for (int i = 0; i < flightToTower.from.size(); i++) {
            this.visitedPoint.add(flightToTower.to.get(i));
            this.angleUsed.add(flightToTower.angle.get(i));
            this.fromPosition.add(flightToTower.from.get(i));
            this.toPosition.add(flightToTower.to.get(i));
            this.DeliverdorderNo.add(null);
        }
        return;
    }

    /**
     * convert 2 longlat to line2D
     * @param p1 input longlat 1
     * @param p2 input longlat 2
     * @return a line2D
     */
    private static Line2D.Double getLine2D(LongLat p1, LongLat p2) {
        Point2D.Double point1 = new Point2D.Double(p1.getLongitude(), p1.getLatitude());
        Point2D.Double point2 = new Point2D.Double(p2.getLongitude(), p2.getLatitude());
        Line2D.Double line = new Line2D.Double(point1, point2);
        return line;
    }

    /**
     * generate the list of deliveries that will be stored in database.
     * @return a list of deliveries
     */
    public ArrayList<Deliveries> generateDeliveries(){
        ArrayList<Orders> orders = this.ordersDeliverd;
        ArrayList<Deliveries> deliveriesList = new ArrayList<Deliveries>();
        for (int i = 0; i < orders.size(); i++) {
            Orders order = orders.get(i);
            Deliveries delivery = new Deliveries(order.orderNo,order.deliverTo,order.cost);
            deliveriesList.add(delivery);
        }
        return deliveriesList;
    }

    /**
     * generate the list of flight path that will be stored in database.
     * @return a list of flight path.
     */
    public ArrayList<FlightPath> generateFlightPath(){
        ArrayList<FlightPath> paths = new ArrayList<FlightPath>();
        for (int i = 0; i < this.angleUsed.size(); i++) {
            FlightPath path = new FlightPath(this.DeliverdorderNo.get(i),this.fromPosition.get(i).longitude(),this.fromPosition.get(i).latitude(),
                    this.angleUsed.get(i),this.toPosition.get(i).longitude(),this.toPosition.get(i).latitude());
            paths.add(path);
        }
        return paths;
    }
}