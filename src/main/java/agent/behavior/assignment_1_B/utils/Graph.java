package agent.behavior.assignment_1_B.utils;

import com.google.gson.GsonBuilder;
import environment.Coordinate;
import environment.Perception;

import java.util.*;
import java.util.List;

import com.google.gson.Gson;

/**
 * A class representing the mapping of the MAS.
 * The mapping is represented by a graph consisting of nodes and edges.
 *
 * Each node can either be a packet, destination or free cell.
 * Each edge between nodes have a cost which is equal to the euclidean distance between the nodes.
 */
public class Graph {

    private HashMap<Coordinate, Node> nodes;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Graph(HashMap<Coordinate, Node> nodes) {
        this.nodes = nodes;
    }

    public Graph() {}

    public Graph(int initialX, int initialY) {
        this.nodes = new HashMap<>();

        Coordinate initialCoordinate = new Coordinate(initialX, initialY);
        Node initialNode = new Node(initialCoordinate, NodeType.FREE);
        nodes.put(initialCoordinate, initialNode);
    }

    /////////////
    // METHODS //
    /////////////

    public Node addNode(Coordinate position, NodeType state) {
        Node node = new Node(position, state);
        nodes.put(position, node);
        
        return node;
    }

    public void addEdge(Coordinate position1, Coordinate position2) {
        Node node1 = new Node(position1, NodeType.FREE);
        Node node2 = new Node(position2, NodeType.FREE);
        
        nodes.get(position1).addEdge(node2.getPosition(), calculateDistance(position1, position2));
        nodes.get(position2).addEdge(node1.getPosition(), calculateDistance(position2, position1));
    }

    public double calculateDistance(Coordinate position1, Coordinate position2) {
        int distanceX = Math.abs(position1.getX() - position2.getX());
        int distanceY = Math.abs(position1.getY() - position2.getY());
        int minDistance = Math.min(distanceX, distanceY);

        // Diagonal distance (minDistance) plus the rest (if distanceX or distanceY is larger than the other)
        return minDistance + Math.abs(distanceX - distanceY);
    }

    // TODO: Check if edge is free bewteen start and end node.
    public Coordinate closestFreeNodeCoordinate(Perception perception, Coordinate position) {
        Coordinate result = null;

        double minDistance = Double.MAX_VALUE;
        for (Coordinate candidatePosition : nodes.keySet()) {
            double candidateDistance = calculateDistance(position, candidatePosition);

            if (perception.getCellPerceptionOnAbsPos(candidatePosition.getX(), candidatePosition.getY()) != null
                && candidateDistance < minDistance
                && nodes.get(candidatePosition).getState() == NodeType.FREE) {

                minDistance = candidateDistance;
                result = candidatePosition;
            }
        }

        return result;
    }

    public boolean nodeExists(int x, int y) {
        Coordinate position = new Coordinate(x, y);

        return nodes.containsKey(position);
    }

    public boolean nodeExists(Coordinate position) {
        return nodes.containsKey(position);
    }

    /**
     * Check if position is on edge using regular linear algebra
     * 
     * @param edgeStart Start of edge
     * @param edgeEnd End of edge
     * @param position Position to be tested if its on the edge
     * @return True if position is on the edge
     */
    public boolean onTheLine(Coordinate edgeStart, Coordinate edgeEnd, Coordinate position) {
        int distanceX = edgeEnd.getX() - edgeStart.getX();
        int distanceY = edgeEnd.getY() - edgeStart.getY();

        int normX = distanceY;
        int normY = -1 * distanceX;
        int D = normX * edgeStart.getX() + normY * edgeStart.getY();
        return normX * position.getX() + normY * position.getY() == D;
    }

    public void removeNode(Coordinate position) {
        Node node = nodes.get(position);

        for (Coordinate edgeCoordinate : node.getEdges().keySet()) {
            nodes.get(edgeCoordinate).deleteEdge(node.getPosition());
        }

        nodes.remove(position);
    }

    public void addEdge(Coordinate position1, Coordinate position2, NodeType state) {
        Node node1 = new Node(position1, NodeType.FREE);
        Node node2 = new Node(position2, state);
        nodes.get(position1).addEdge(node2.getPosition(), calculateDistance(position1, position2));
        nodes.get(position2).addEdge(node1.getPosition(), calculateDistance(position2, position1));
    }

    public List<Coordinate> doSearch(Coordinate startPosition, Coordinate endPosition) {
        PriorityQueue<PathNode> unsettledNodes = new PriorityQueue<>();
        Set<Coordinate> visitedPositions = new HashSet<>();

        PathNode sourcePathNode = new PathNode(nodes.get(startPosition).getPosition(), 0);
        unsettledNodes.add(sourcePathNode);

        while (unsettledNodes.size() != 0) {
            PathNode currentPathNode = unsettledNodes.poll();
            visitedPositions.add(currentPathNode.getPosition());

            if (currentPathNode.getPosition().equals(endPosition)) {
                return generateNodePath(currentPathNode);
            }

            HashMap<Coordinate, Double> neighbours = nodes.get(currentPathNode.getPosition()).getEdges();

            for (Coordinate neigbourPosition : neighbours.keySet()) {
                
                // totalCost = cost to current node + cost of edge bewteen current node and neighbour
                // TODO: Maybe implement A* here
                if (!visitedPositions.contains(neigbourPosition)) {
                    double totalCost = currentPathNode.getCost() + neighbours.get(neigbourPosition);
                    PathNode newPathNode = new PathNode(neigbourPosition, totalCost);
                    newPathNode.setCheapestPreviousNode(currentPathNode);
                    unsettledNodes.add(newPathNode);
                }
            }
        }

        // If no path exists
        return new ArrayList<>();
    }


    private List<Coordinate> generateNodePath(PathNode currentNode) {

        if (currentNode.getCheapestPreviousNode() == null) {
            return new LinkedList<>();
        }
        else
        {
            List<Coordinate> result = generateNodePath(currentNode.getCheapestPreviousNode());
            Coordinate pathTail = currentNode.getCheapestPreviousNode().getPosition();
            if (!result.isEmpty())
                pathTail = result.get(result.size() - 1);

            Coordinate nextCoordinate = currentNode.getPosition();
            List<Coordinate> path = generatePathPoints(pathTail, nextCoordinate);
            result.addAll(path);
            return result;
        }
    }

    private List<Coordinate> generatePathPoints(Coordinate startPosition, Coordinate endPosition) {
        int dX = endPosition.getX() - startPosition.getX();
        int dY = endPosition.getY() - startPosition.getY();
        int numDiagSteps = Math.min(Math.abs(dX), Math.abs(dY));
        int dxStep = dX > 0 ? 1 : (dX < 0 ? -1 : 0);
        int dyStep = dY > 0 ? 1 : (dY < 0 ? -1 : 0);

        List<Coordinate> result = new LinkedList<>();
        Coordinate lastCoordinate = startPosition;
        for (int i = 1; i <= numDiagSteps; i++) {
            lastCoordinate = new Coordinate(startPosition.getX() + i * dxStep, startPosition.getY() + i * dyStep);
            result.add(lastCoordinate);
        }

        int dxxStep = Math.abs(dX) > Math.abs(dY)  ? dxStep : 0;
        int dyxStep = Math.abs(dY)  > Math.abs(dX)  ? dyStep : 0;
        int diffSteps = Math.abs(Math.abs(dX) - Math.abs(dY));

        for (int i = 1; i <= diffSteps; i++) {
            result.add(new Coordinate(lastCoordinate.getX() + i*dxxStep, lastCoordinate.getY() + i*dyxStep));
        }

        return result;
    }

    //////////
    // JSON //
    //////////

    public String toJson() {
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.toJson(this);
    }

    public static Graph fromJson(String graphString) {
        // TODO
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(graphString, Graph.class);
    }
}