package util.mapping;

import environment.Coordinate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Node {
    private Color color;
    private Coordinate position;
    private String type = "free";
    private int hashCode;

    private HashMap<Node, Double> edges = new HashMap<>();

    public Node(Coordinate position) {
        this.position = position;
        this.hashCode = Objects.hash(position);
    }

    public Node(Coordinate position, String type) {
        this(position);
        this.type = type;
        this.color = color;
    }

    public Node(Coordinate position, String type, Color color) {
        this(position);
        this.type = type;
        this.color = color;
    }

    public int getX() {
        return position.getX();
    }

    public int getY() {
        return position.getY();
    }

    @Override
    public String toString() {
        return "Node{" +
                "color=" + color +
                ", position=" + position +
                ", type='" + type + '\'' +
                '}';
    }

    public Coordinate getPosition() {
        return this.position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node that = (Node) o;
        return position.getX() == that.position.getX() && position.getY() == that.position.getY();
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    public HashMap<Node, Double> getEdges() {
        return edges;
    }

    public void deleteEdge(Node other) {
        edges.remove(other);
    }

    public void addEdge(Node other, double cost) {
        this.edges.put(other, cost);
    }

    public void setType(String type) {
        this.type = type;
    }
}
