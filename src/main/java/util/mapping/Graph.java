package util.mapping;

import environment.Coordinate;
import environment.Perception;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A class representing the mapping of the MAS.
 * The mapping is represented by a graph consisting of nodes and edges.
 *
 * Each node can either be a packet, destination or free cell.
 * Each edge between nodes have a cost which is equal to the euclidean distance between the nodes.
 *
 */
public class Graph {
    private HashMap<Coordinate, Node> nodes = new HashMap<>();

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
        return n;
    }

    public Node addPacketNode(Coordinate p, Color color) {
        Node n = new Node(p, "packet", color);
        nodes.put(p, n);
        return n;
    }

    public void addEdge(Coordinate c1, Coordinate c2) {
        Node n1 = new Node(c1);
        Node n2 = new Node(c2);
        nodes.get(c1).addEdge(n2, distance(c1, c2));
        nodes.get(c2).addEdge(n1, distance(c2, c1));
    }

    public double distance(Coordinate c1, Coordinate c2) {
        int distX = Math.abs(c2.getX() - c1.getX());
        int distY = Math.abs(c2.getY() - c1.getY());
        int minDist = Math.min(distX, distY);

        // Diagonal distance (minDist) plus the rest (if distX or distY is larger than the other)
        return minDist + Math.abs(distX - distY);

    }

    public Coordinate closestFreeNodeCoordinate(Perception perception, Coordinate c0) {
        // TODO: Check if edge is free bewteen start and end node.
        Coordinate closestCoord = null;
        double closestDistance = Double.MAX_VALUE;
        for (Coordinate c : nodes.keySet()) {
            double dist = distance(c0, c);
            if (perception.getCellPerceptionOnAbsPos(c.getX(), c.getY()) != null
                    && dist < closestDistance
                    && nodes.get(c).getType().equals("free")) {
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

    /**
     * Just your regular linear algebra.
     * @param edgeStart Start of line.
     * @param edgeEnd End of line.
     * @param p Point to be tested if its on the line.
     * @return True if p is on the line.
     */
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
            nodes.get(other.getPosition()).deleteEdge(n);
        }
        nodes.remove(c);
    }

    public void addEdge(Coordinate c1, Coordinate c2, String type, Color color) {
        Node n1 = new Node(c1);
        Node n2 = new Node(c2, type, color);
        nodes.get(c1).addEdge(n2, distance(c1, c2));
        nodes.get(c2).addEdge(n1, distance(c2, c1));
    }

    public List<Coordinate> doSearch(Coordinate start, Coordinate end) {
        PriorityQueue<PathNode> unsettledNodes = new PriorityQueue<>();
        Set<Coordinate> visited = new HashSet<>();

        PathNode source = new PathNode(nodes.get(start).getPosition(), 0);
        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            PathNode currentNode = unsettledNodes.poll();
            visited.add(currentNode.getPosition());

            if (currentNode.getPosition().equals(end)) {
                return generateNodePath(currentNode);
            }

            HashMap<Node, Double> neghbours = nodes.get(currentNode.getPosition()).getEdges();

            for (Node neigbour : neghbours.keySet()) {
                // totalCost = cost to current node + cost of edge bewteen current node and neighbour
                // TODO: Maybe implement Astar here
                if (!visited.contains(neigbour.getPosition())) {
                    double totalCost = currentNode.getCost() + neghbours.get(neigbour);
                    PathNode newNode = new PathNode(neigbour.getPosition(), totalCost);
                    newNode.setCheapestPreNode(currentNode);
                    unsettledNodes.add(newNode);
                }
            }
        }

        // If no path exists
        return new ArrayList<>();
    }


    private List<Coordinate> generateNodePath(PathNode currentNode) {

        if (currentNode.getCheapestPreNode() == null) {
            return new LinkedList<>();
        }
        else
        {
            List<Coordinate> result = generateNodePath(currentNode.getCheapestPreNode());
            Coordinate pathTail = currentNode.getCheapestPreNode().getPosition();
            if (!result.isEmpty())
                pathTail = result.get(result.size() - 1);

            Coordinate nextCoordinate = currentNode.getPosition();
            List<Coordinate> path = generatePathPoints(pathTail, nextCoordinate);
            result.addAll(path);
            return result;
        }
    }

    private List<Coordinate> generatePathPoints(Coordinate start, Coordinate end) {
        int dx = end.getX() - start.getX();
        int dy = end.getY() - start.getY();
        int numDiagSteps = Math.min(Math.abs(dx), Math.abs(dy));
        int dxStep = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
        int dyStep = dy > 0 ? 1 : (dy < 0 ? -1 : 0);

        List<Coordinate> result = new LinkedList<>();
        Coordinate lastCoordinate = start;
        for (int i = 1; i <= numDiagSteps; i++) {
            lastCoordinate = new Coordinate(start.getX() + i*dxStep, start.getY() + i*dyStep);
            result.add(lastCoordinate);
        }

        int dxxStep = Math.abs(dx) > Math.abs(dy)  ? dxStep : 0;
        int dyxStep = Math.abs(dy)  > Math.abs(dx)  ? dyStep : 0;
        int diffSteps = Math.abs(Math.abs(dx) - Math.abs(dy));

        for (int i = 1; i <= diffSteps; i++) {
            result.add(new Coordinate(lastCoordinate.getX() + i*dxxStep, lastCoordinate.getY() + i*dyxStep));
        }

        return result;
    }
}

class PathNode implements Comparable {
    private Coordinate position;
    private double cost;
    private PathNode cheapestPreNode;

    public PathNode(Coordinate position, double cost) {
        this.position = position;
        this.cost = cost;
    }

    public PathNode getCheapestPreNode() {
        return cheapestPreNode;
    }

    public void setCheapestPreNode(PathNode cheapestPreNode) {
        this.cheapestPreNode = cheapestPreNode;
    }

    public double getCost() {
        return cost;
    }

    @Override
    public int compareTo(Object o) {
        PathNode other = (PathNode) o;
        return (int) (this.getCost() - other.getCost());
    }

    public Coordinate getPosition() {
        return position;
    }
}