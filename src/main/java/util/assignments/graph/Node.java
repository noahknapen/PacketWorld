package util.assignments.graph;

import environment.Coordinate;

/**
 * A class that represents a node
 */
public class Node {

    private Coordinate coordinate;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Node(Coordinate coordinate) {
        this.setCoordinate(coordinate);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return coordinate.toString();
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Node node) {
            result = this.coordinate.equals(node.getCoordinate());
        }

        return result;
    }

    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }
}
