package util.assignments.graph;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import environment.Coordinate;

/**
 * A class that represents a node
 */
@JsonIgnoreProperties(value={"fcost"})
public class Node implements Comparable<Node> {

    private Coordinate coordinate;
    private double gCost;
    private double hCost;
    private Node parent;


    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Node(Coordinate coordinate) {
        this.setCoordinate(coordinate);
    }

    @JsonCreator
    public Node(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("gcost") double gCost, @JsonProperty("hcost") double hCost, @JsonProperty("parent") Node parent) {
        this.coordinate = coordinate;
        this.gCost = gCost;
        this.hCost = hCost;
        this.parent = parent;
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }
    
    public double getFCost() {
        return gCost + hCost;
    }

    public Node getParent() {
        return parent;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setGCost(double gCost) {
        this.gCost = gCost;
    }

    public void setHCost(double hCost) {
        this.hCost = hCost;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    /////////////
    // METHODS //
    /////////////

    public void resetPathFindingVariables() {
        this.setGCost(Double.MAX_VALUE);
        this.setHCost(Double.MAX_VALUE);
        this.setParent(null);
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s %s", coordinate, gCost, hCost, parent);
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

    @Override
    public int compareTo(Node node) {
        return Double.compare(this.getFCost(), node.getFCost());
    }
}
