package util.assignments.graph;

import java.util.Optional;

import com.fasterxml.jackson.annotation.*;

import environment.Coordinate;
import util.assignments.targets.Packet;
import util.assignments.targets.Target;

/**
 * A class representing a node
 */
@JsonIgnoreProperties(value={"walkable", "fcost"})
public class Node implements Comparable<Node> {

    // A data member holding the coordinate of the node
    private Coordinate coordinate;
    // A data member holding the optional target of the node
    private Optional<Target> target;
    // A data member holding the path cost of the node
    private double gCost;
    // A data member holding the heuristic cost of the node
    private double hCost;
    // A data member holding the parent of the node
    private Node parent;
    // A data member holding the time at which the node was updated
    private long updateTime;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Node(Coordinate coordinate) {
        this.setCoordinate(coordinate);
        this.setGCost(0);
        this.setHCost(0);
        this.setParent(null);
        this.setUpdateTime(System.currentTimeMillis());
    }

    public Node(Coordinate coordinate, Optional<Target> target) {
        this(coordinate);
        this.setTarget(target);
    }

    @JsonCreator
    public Node(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("target") Optional<Target> target, @JsonProperty("gCost") double gCost, @JsonProperty("hCost") double hCost, @JsonProperty("parent") Node parent, @JsonProperty("updateTime") long updateTime) {
        this.setCoordinate(coordinate);
        this.setTarget(target);
        this.setGCost(gCost);
        this.setHCost(hCost);
        this.setParent(parent);
        this.setUpdateTime(updateTime);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Optional<Target> getTarget() {
        return target;
    }
    
    /**
     * Is the node walkable?
     * 
     * @return True if the node is walkable, otherwise false
     */
    public boolean isWalkable() {
        return this.target.isEmpty();
    }

    public double getGCost() {
        return gCost;
    }

    public double getHCost() {
        return hCost;
    }

    /**
     * Get the total cost of the node
     * 
     * @return The total cost of the node
     */
    public double getFCost() {
        return gCost + hCost;
    }

    public Node getParent() {
        return parent;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setTarget(Optional<Target> target) {
        this.target = target;
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

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Does the node contain a target?
     * 
     * @return True if the node contains a target, otherwise false
     */
    public boolean containsTarget() {
        return target.isPresent();
    }

    /**
     * Does the node contain a packet?
     * 
     * @return True if the node contains a packet, otherwise false
     */
    public boolean containsPacket() {
        return containsTarget() && target.get().getClass().equals(Packet.class);
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", coordinate, target, gCost, hCost, parent, updateTime);
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