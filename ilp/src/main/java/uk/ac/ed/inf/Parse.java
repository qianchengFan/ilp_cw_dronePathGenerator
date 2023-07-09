package uk.ac.ed.inf;

import java.sql.*;
import java.util.ArrayList;

/**
 * This class provides helper function that used to communicate with database, either get data from it or write
 * data to it.
 */
public class Parse {
    /**
     * This method is to get required information of all orders at this day, return as a list of orders.
     * @return The list of all orders
     * @throws SQLException SQLException
     */
    public static ArrayList<Orders> getOrderTable() throws SQLException {
        //first we construct a jdbc-string to access the database.
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);

        //statement for accessing orders table
        String ordersQuery = "select * from orders where deliveryDate=(?)";
        PreparedStatement psOrderQuery = conn.prepareStatement(ordersQuery);

        //statement for accessing orderDetails table
        String ordersDetails = "select * from orderDetails where orderNo=(?)";
        PreparedStatement psOrderDetails = conn.prepareStatement(ordersDetails);

        //initialize the orderList to store order information later
        ArrayList<Orders> orderList = new ArrayList<>();

        //get orders at the input date
        psOrderQuery.setString(1, Drone.date);
        ResultSet rs = psOrderQuery.executeQuery();

        //loop through all the orders, and record their information that we need.
        while (rs.next()) {
            //get the order's order number and delivery address
            String orderNo = rs.getString("orderNo");
            String deliverTo = rs.getString("deliverTo");
            //get delivery items of this order by the input order number
            psOrderDetails.setString(1, orderNo);
            ResultSet rs2 = psOrderDetails.executeQuery();
            //and initialize the itemList to store the items that need to be delivered in this order.
            ArrayList<String> itemList = new ArrayList<>();
            //Loop through items we get
            while (rs2.next()){
                String item = rs2.getString("item");
                //add matched item to the item List.
                itemList.add(item);
            }
            //Store all information in this order as an order(the class), and add it to the order list.
            Orders temp = new Orders(orderNo,deliverTo,itemList);
            orderList.add(temp);
        }
        return orderList;
    }

    /**
     * Initialize the deliveries table in database
     * @throws SQLException SQLException
     */
    public static void createDeliveriesTable() throws SQLException {
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();
        statement.execute(
                "create table deliveries(" +
                        "orderNo char(8), " +
                        "deliveredTo varchar(19), " +
                        "costInPence int)");
    }


    /**
     * Initialize the flight path table in database
     * @throws SQLException SQLException
     */
    public static void createFlightPath() throws SQLException {
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();
        statement.execute(
                "create table flightpath(" +
                        "orderNo char(8), " +
                        "fromLongitude double, " +
                        "fromLatitude double, " +
                        "angle integer, " +
                        "toLongitude double, " +
                        "toLatitude double)");
    }

    /**
     * delete the flight path table in database.
     * @throws SQLException SQLException
     */
    public static void dropFlightPath() throws SQLException{
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();
        DatabaseMetaData databaseMetadata = conn.getMetaData();
        ResultSet resultSet = databaseMetadata.getTables(null, null, "FLIGHTPATH", null);
        if (resultSet.next()) {
            statement.execute("drop table flightpath");
        }
    }

    /**
     * delete the deliveries table in database
     * @throws SQLException SQLException
     */
    public static void dropDeliveriesTable() throws SQLException{
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        Statement statement = conn.createStatement();
        DatabaseMetaData databaseMetadata = conn.getMetaData();
        ResultSet resultSet =
                databaseMetadata.getTables(null, null, "DELIVERIES", null);
        if (resultSet.next()) {
            statement.execute("drop table deliveries");
        }
    }


    /**
     * insert data into deliveries table in database
     * @param deliveriesList the list of deliveries that we want to insert
     * @throws SQLException SQLException
     */
    public static void insertToDeliveries(ArrayList<Deliveries> deliveriesList) throws SQLException {
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        PreparedStatement psDelivery = conn.prepareStatement(
                "insert into deliveries values (?, ?, ?)");

        //loop through all the deliveries that need to insert and insert them to the table.
        for(Deliveries o:deliveriesList){
            psDelivery.setString(1, o.orderNo);
            psDelivery.setString(2, o.deliveredTo);
            psDelivery.setInt(3, o.costInPence);
            psDelivery.execute();
        }
    }

    /**
     * insert data into flight path table in database
     * @param paths the list of flight path that we want to insert
     * @throws SQLException SQLException
     */
    public static void insertToFlightPath(ArrayList<FlightPath> paths) throws SQLException {
        String jdbcString = "jdbc:derby://localhost:"+Drone.databasePort+"/derbyDB";
        Connection conn = DriverManager.getConnection(jdbcString);
        PreparedStatement psPath = conn.prepareStatement(
                "insert into flightpath values (?, ?, ?, ?, ?, ?)");

        //loop through all the flight path that need to insert and insert them to the table.
        for(FlightPath o:paths){
            psPath.setString(1, o.orderNo);
            psPath.setDouble(2, o.fromLongitude);
            psPath.setDouble(3, o.fromLatitude);
            psPath.setInt(4, o.angle);
            psPath.setDouble(5, o.toLongitude);
            psPath.setDouble(6, o.toLatitude);
            psPath.execute();
        }
    }



}
