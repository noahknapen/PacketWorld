package agent.behavior.assignment_1_B.utils;

import environment.Coordinate;

import java.util.*;

public class Node {
    
    private Coordinate position;
    private NodeState state;
    private HashMap<Node, Double> edges;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public Node(Coordinate position, NodeState state) {
        this.position = position;
        this.state = state;
        this.edges = new HashMap<>();
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Coordinate getPosition() {
        return this.position;
    }

    public NodeState getState() {
        return state;
    }

    public HashMap<Node, Double> getEdges() {
        return edges;
    }

    public void setState(NodeState state) {
        this.state = state;
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

    /////////////
    // METHODS //
    /////////////

    public void addEdge(Node node, double cost) {
        this.edges.put(node, cost);
    }

    public void deleteEdge(Node node) {
        edges.remove(node);
    }
}

