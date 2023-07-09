package uk.ac.ed.inf;

import com.mapbox.geojson.Point;

import java.util.List;

/**
 * it's the implementation of flight class, flight is used to store information about move from a position to another position.
 */
public class Flight {
    /**
     * The number of moves this flight take
     */
    public Integer moves;
    /**
     * The list of from position of each move in this flight
     */
    public List<Point> from;
    /**
     * The list of to position of each move in this flight
     */
    public List<Point> to;
    /**
     * The list of angle of each move in this flight
     */
    public List<Integer> angle;
    /**
     * The list of visited point in this flight
     */
    public List<Point> path;
    /**
     * The Integer that act as a signal for other method.
     */
    public Integer tempIndex;

    /**
     * Flight's constructor
     * @param moves The number of moves this flight take
     * @param path The Integer that act as a signal for other method.
     * @param from The list of from position of each move in this flight
     * @param to The list of to position of each move in this flight
     * @param angle The list of angle of each move in this flight
     */
    public Flight(Integer moves, List<Point> path , List<Point> from,List<Point> to,List<Integer> angle) {
        this.moves = moves;
        this.path = path;
        this.from = from;
        this.to = to;
        this.angle = angle;
        this.tempIndex = 0;
    }

    /**
     * @return total number of moves of this flight
     */
    public Integer getMoves() {
        return moves;
    }


    /**
     * @return the list of point that this flight passed.
     */
    public List<Point> getPath() {
        return path;
    }
}
