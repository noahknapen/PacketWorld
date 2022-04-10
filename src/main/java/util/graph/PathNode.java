package util.graph;

import com.google.gson.Gson;

import environment.Coordinate;

/**
 * A class representing a path node
 */
class PathNode implements Comparable<PathNode> {

    private Coordinate position;
    private double cost;
    private PathNode cheapestPreviousNode;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public PathNode(Coordinate position, double cost) {
        this.position = position;
        this.cost = cost;
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getPosition() {
        return position;
    }

    public double getCost() {
        return cost;
    }

    public PathNode getCheapestPreviousNode() {
        return cheapestPreviousNode;
    }

    public void setCheapestPreviousNode(PathNode cheapestPreviousNode) {
        this.cheapestPreviousNode = cheapestPreviousNode;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public int compareTo(PathNode pathNode) {
        return (int) (this.getCost() - pathNode.getCost());
    }

    //////////
    // JSON //
    //////////

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static PathNode fromJson(String pathNodeString) {
        Gson gson = new Gson();
        return gson.fromJson(pathNodeString, PathNode.class);
    }
}

