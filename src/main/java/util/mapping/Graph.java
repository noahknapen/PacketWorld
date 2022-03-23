package util.mapping;

import environment.Coordinate;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph {
    private HashMap<Coordinate, Node> nodes = new HashMap<>();
    private List<Node> packets = new ArrayList<>();
    private List<Node> destinations = new ArrayList<>();


    public Graph(int initX, int initY) {
        Coordinate initCoordinate = new Coordinate(initX, initY);
        Node initNode = new Node(initCoordinate);
        nodes.put(initCoordinate, initNode);
    }

    public Node addFreeNode(Coordinate p) {
        Node n = new Node(p);
        nodes.put(p, n);
        return n;
    }

    public Node addDestinationNode(Coordinate p, Color color) {
        Node n = new Node(p, "destination", color);
        nodes.put(p, n);
        destinations.add(n);
        return n;
    }

    public Node addPacketNode(Coordinate p, Color color) {
        Node n = new Node(p, "packet", color);
        nodes.put(p, n);
        packets.add(n);
        return n;
    }

    public void deleteNode(Coordinate p) {
        Node n = new Node(p);
        nodes.remove(n);
    }



    public void addNodes(List<Coordinate> coords) {
        for (Coordinate coord : coords) {

        }
    }

    public void addEdge(Coordinate c1, Coordinate c2) {
        Node n1 = new Node(c1);
        Node n2 = new Node(c2);
        nodes.get(c1).addEdge(n2, distance(c1, c2));
        nodes.get(c2).addEdge(n1, distance(c2, c1));
    }

    public double distance(Coordinate c1, Coordinate c2) {
        int distX = c2.getX() - c1.getX();
        int distY = c2.getY() - c1.getY();
        int minDist = Math.min(distX, distY);

        // Diagonal distance (minDist) plus the rest (if distX or distY is larger than the other)
        return minDist + Math.abs(distX - distY);

    }

    private List<Edge> getNodes() {
        Edge e = new Edge(new Node(new Coordinate(1,2)), new Node(new Coordinate(3,4)), 2);
        List<Edge> eList = new ArrayList<>();
        eList.add(e);
        return eList;
    }

    public Coordinate closestCoordinate(List<Coordinate> coords, Coordinate c0) {
        // TODO: Check if edge is free bewteen start and end node.
        Coordinate closestCoord = coords.get(0);
        double closestDistance = distance(c0, closestCoord);
        for (Coordinate c : coords) {
            double dist = distance(c0, c);
            if (dist < closestDistance) {
                closestCoord = c;
                closestDistance = dist;
            }
        }
        return closestCoord;
    }

    public boolean nodeExists(int x, int y) {
        return nodes.containsKey(new Coordinate(x, y));
    }

    public boolean nodeExists(Coordinate c) {
        return nodes.containsKey(c);
    }

    public boolean onTheLine(Coordinate edgeStart, Coordinate edgeEnd, Coordinate p) {
        int distX = edgeEnd.getX() - edgeStart.getX();
        int distY = edgeEnd.getY() - edgeStart.getY();

        int normX = distY;
        int normY = -1*distX;
        int D = normX*edgeStart.getX() + normY*edgeStart.getY();
        boolean res = normX * p.getX() + normY*p.getY() == D;
        return res;
    }

    public void removePacketNode(Coordinate c) {
        Node n = nodes.get(c);

        for (Node other : n.getEdges().keySet()) {
            other.deleteEdge(n);
        }
    }

    public void setType(Coordinate c, String type) {
        nodes.get(c).setType(type);
    }

    public void addEdge(Coordinate c1, Coordinate c2, String type, Color color) {
        Node n1 = new Node(c1);
        Node n2 = new Node(c2, type, color);
        nodes.get(c1).addEdge(n2, distance(c1, c2));
        nodes.get(c2).addEdge(n1, distance(c2, c1));
    }
}
