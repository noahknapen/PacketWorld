package agent.behavior.assignment_1_B.utils;

import environment.Coordinate;

import java.util.*;

public class Node {
    
    private Coordinate position;
    private NodeType type;
    private HashMap<Coordinate, Double> edges;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Node(Coordinate position, NodeType type, HashMap<Coordinate, Double> edges) {
        this.position = position;
        this.type = type;
        this.edges = edges;
    }

    public Node(Coordinate position, NodeType type) {
        this.position = position;
        this.type = type;
        this.edges = new HashMap<>();
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getPosition() {
        return this.position;
    }

    public NodeType getType() {
        return type;
    }

    public HashMap<Coordinate, Double> getEdges() {
        return edges;
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Node) {
            Node node = (Node) object;
            result = node.getPosition().equals(this.getPosition());
        }

        return result;
    }

    @Override
    public String toString() {
        return "Node{" +
                "position=" + position +
                ", type=" + type +
                ", edges=" + edges +
                '}';
    }

    /////////////
    // METHODS //
    /////////////

    public void addEdge(Coordinate coordinate, double cost) {
        this.edges.put(coordinate, cost);
    }

    public void deleteEdge(Coordinate coordinate) {
        edges.remove(coordinate);
    }
}

