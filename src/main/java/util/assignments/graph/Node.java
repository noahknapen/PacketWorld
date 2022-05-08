package util.assignments.graph;

import com.fasterxml.jackson.annotation.*;

import environment.Coordinate;
import util.assignments.targets.Target;

/**
 * A class that represents a node
 */

@JsonIgnoreProperties(value={"fcost"})
public class Node implements Comparable<Node> {

    private Coordinate coordinate;
    private double gCost;
    private double hCost;
    private Node parent;

    private Target target;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Node(Coordinate coordinate) {
        this.setCoordinate(coordinate);
        this.setGCost(0);
        this.setHCost(0);
        this.setParent(null);
    }

    public Node(Coordinate coordinate, Target target) {
        this(coordinate);
        this.setTarget(target);
    }

    @JsonCreator
    public Node(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("gcost") double gCost, @JsonProperty("hcost") double hCost, @JsonProperty("parent") Node parent, @JsonProperty("target") Target target) {
        this.coordinate = coordinate;
        this.gCost = gCost;
        this.hCost = hCost;
        this.parent = parent;
        this.target = target;
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

    public boolean nodeWalkable() {
        return this.target == null;
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

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s", coordinate, gCost, hCost, parent, target);
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
    public int compareTo(Node node) {
        return Double.compare(this.getFCost(), node.getFCost());
    }

    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }


}
