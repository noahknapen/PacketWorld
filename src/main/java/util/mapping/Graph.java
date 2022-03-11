package util.mapping;

import environment.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph {
    private HashMap<Node, List<Edge>> edges = new HashMap<>();

    public Graph(int initX, int initY) {
        Node initNode = new Node(new Coordinate(initX, initY));
        edges.put(initNode, new ArrayList<>());
    }

    public void addNode(Coordinate p) {

    }

    public void addEdge(Node n1, Node n2) {

    }

    public double distance(Node n1, Node n2) {
        return 0;
    }

    private List<Edge> getEdges() {
        Edge e = new Edge(new Node(new Coordinate(1,2)), new Node(new Coordinate(3,4)), 2);
        List<Edge> eList = new ArrayList<>();
        eList.add(e);
        return eList;
    }
}
