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

    public Node addNode(Coordinate p) {
        Node n = new Node(p);
        edges.put(n, new ArrayList<>());
        return n;
    }

    public void deleteNode(Coordinate p) {
        Node n = new Node(p);
        edges.remove(n);
    }


    public void addNodes(List<Coordinate> coords) {
        for (Coordinate coord : coords) {

        }
    }

    public void addEdge(Coordinate c1, Coordinate c2) {
        Node n1 = new Node(c1);
        Node n2 = new Node(c2);
        edges.get(n1).add(new Edge(n1, n2, distance(n1, n2)));
        edges.get(n2).add(new Edge(n2, n1, distance(n2, n1)));
    }

    public double distance(Node n1, Node n2) {
        int distX = n2.getX() - n1.getX();
        int distY = n2.getY() - n1.getY();
        int minDist = Math.min(distX, distY);

        // Diagonal distance (minDist) plus the rest (if distX or distY is larger than the other)
        return minDist + Math.abs(distX - distY);

    }

    private List<Edge> getEdges() {
        Edge e = new Edge(new Node(new Coordinate(1,2)), new Node(new Coordinate(3,4)), 2);
        List<Edge> eList = new ArrayList<>();
        eList.add(e);
        return eList;
    }

    public Node closestNode(List<Node> nodeList, Node n) {
        Node closestNode = nodeList.get(0);
        double closestDistance = distance(n, closestNode);
        for (Node node : nodeList) {
            double dist = distance(n, node);
            if (dist < closestDistance) {
                closestNode = node;
                closestDistance = dist;
            }
        }
        return closestNode;
    }

    public boolean nodeExists(int x, int y) {
        Node n = new Node(new Coordinate(x, y));
        if (edges.containsKey(n)) {
            return true;
        }
        return false;
    }

    public boolean nodeExists(Coordinate c) {
        Node n = new Node(c);
        if (edges.containsKey(n)) {
            return true;
        }
        return false;
    }

    public boolean onTheLine(Coordinate edgeStart, Coordinate edgeEnd, Coordinate p) {
        int distX = edgeEnd.getX() - edgeStart.getX();
        int distY = edgeEnd.getY() - edgeStart.getY();

        int normX = distY;
        int normY = -1*distX;
        int D = normX*edgeStart.getX() + normY*edgeStart.getY();
        boolean res = normX* p.getX() + normY*p.getY() == D;
        return res;



    }
}
