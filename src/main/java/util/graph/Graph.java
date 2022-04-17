package util.graph;

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
 * Each node can either be a packet, destination, battery or free cell.
 * Each edge between nodes have a cost which is equal to the euclidean distance between the nodes.
 */
public class Graph {

    private HashMap<Coordinate, Node> nodes;
    private List<Coordinate> currentPath = new ArrayList<>();

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public Graph(HashMap<Coordinate, Node> nodes) {
        this.nodes = nodes;
    }

    public Graph() {}

    public Graph(int initialX, int initialY) {
        // Create an empty map containing all the nodes
        this.nodes = new HashMap<>();

        // Creating and adding the initial node
        Coordinate initialCoordinate = new Coordinate(initialX, initialY);
        this.addNode(initialCoordinate, NodeType.FREE);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * A function to add a node to the graph.
     *
     * @param position: The coordinates of the cell in the virtual world from which we try to make a node
     * @param type: The type a cell it is
     *
     */
    public void addNode(Coordinate position, NodeType type) {
        Node node = new Node(position, type);
        nodes.put(position, node);
    }

    /**
     * A function to add an edge between the given positions
     *
     * @param position1: The virtual world position of the first node
     * @param position2: The virtual world position of the second node
     */
    public void addEdge(Coordinate position1, Coordinate position2) {
        // Add the edge to the first node
        retrieveNode(position1).addEdge(position2, calculateDistance(position1, position2));

        // Add the edge to the second node
        retrieveNode(position2).addEdge(position1, calculateDistance(position2, position1));
    }

    /**
     * A function that retrieves the node with the given virtual world coordinates
     *
     * @param coordinate: The virtual world coordinates of the requested node
     *
     * @return The requested node
     */
    public Node retrieveNode(Coordinate coordinate) {

        // A guard clause to ensure the node exists, otherwise create a new node
        if(!nodeExists(coordinate)) return null;

        // Return the requested node, cannot be null
        return nodes.get(coordinate);
    }

    public HashMap<Coordinate, Node> getNodes() {
        return nodes;
    }

    /**
     * A function to calculate the distance between two positions
     *
     * @param position1: The first position
     * @param position2: The second position
     *
     * @return A double representing the distance
     */
    public double calculateDistance(Coordinate position1, Coordinate position2) {
        // Calculate the difference in the X-position
        int distanceX = Math.abs(position1.getX() - position2.getX());

        // Calculate the difference in the Y-position
        int distanceY = Math.abs(position1.getY() - position2.getY());

        // Take the minimal distance of them both
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

    /**
     * A function to check if the node constructed from the given coordinates exists in the graph structure
     *
     * @param x: The x coordinate of the node in the virtual world
     * @param y: The y coordinate of the node in the virtual world
     *
     * @return true if a node exists, int the graph, with those coordinates
     */
    public boolean nodeExists(int x, int y) {
        Coordinate position = new Coordinate(x, y);
        return nodes.containsKey(position);
    }
    /**
     * A function to check if the node constructed from the given coordinates exists in the graph structure
     *
     * @param position: The coordinates of the node in the virtual world
     *
     * @return true if a node exists, int the graph, with those coordinates
     */
    public boolean nodeExists(Coordinate position) {
        return nodes.containsKey(position);
    }

    /**
     * Check if the position is on the edge between the start coordinates and the end coordinates using regular
     * linear algebra
     * 
     * @param edgeStart Start of edge
     * @param edgeEnd End of edge
     * @param position Position to be tested if it's on the edge
     *
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

    /**
     * A function that removes the node represented by the given coordinates from the graph
     *
     * @param position: The virtual world coordinates of the node
     */
    public void removeNode(Coordinate position) {
        // Make a node from the coordinates
        Node node = this.retrieveNode(position);

        // Iterate over every node which has an edge with the to delete node and remove the edge
        for (Coordinate edgeCoordinate : node.getEdges().keySet()) {
            this.retrieveNode(edgeCoordinate).deleteEdge(node.getPosition());
        }

        // Finally, remove the node from the graph
        nodes.remove(position);
    }

    /**
     * A search algorithm, don't understand it fully so matbe someone else? TODO: document because hard to understand
     *
     */
    public List<Coordinate> doSearch(Coordinate startPosition, Coordinate endPosition) {
        PriorityQueue<PathNode> unsettledNodes = new PriorityQueue<>();
        Set<Coordinate> visitedPositions = new HashSet<>();

        PathNode sourcePathNode = new PathNode(retrieveNode(startPosition).getPosition(), 0);
        unsettledNodes.add(sourcePathNode);

        while (unsettledNodes.size() != 0) {
            PathNode currentPathNode = unsettledNodes.poll();
            visitedPositions.add(currentPathNode.getPosition());

            if (currentPathNode.getPosition().equals(endPosition)) return generateNodePath(currentPathNode);

            HashMap<Coordinate, Double> neighbours = retrieveNode(currentPathNode.getPosition()).getEdges();

            for (Coordinate neighbourPosition : neighbours.keySet()) {
                // totalCost = cost to current node + cost of edge between current node and neighbour
                // TODO: Maybe implement A* here
                if (!visitedPositions.contains(neighbourPosition)) {
                    double totalCost = currentPathNode.getCost() + neighbours.get(neighbourPosition);
                    PathNode newPathNode = new PathNode(neighbourPosition, totalCost);
                    newPathNode.setCheapestPreviousNode(currentPathNode);
                    unsettledNodes.add(newPathNode);
                }
            }
        }

        // If no path exists
        return new ArrayList<>();
    }

    /**
     * A search algorithm, don't understand it fully so matbe someone else? TODO: document because hard to understand
     *
     */
    private List<Coordinate> generateNodePath(PathNode currentNode) {

        // A guard clause to check if there is a cheapestPreviousNode
        if (currentNode.getCheapestPreviousNode() == null) return new LinkedList<>();

        // A recursive call to get a list of coordinates forming a path
        List<Coordinate> result = generateNodePath(currentNode.getCheapestPreviousNode());
        Coordinate pathTail = currentNode.getCheapestPreviousNode().getPosition();

        if (!result.isEmpty()) pathTail = result.get(result.size() - 1);

        Coordinate nextCoordinate = currentNode.getPosition();
        List<Coordinate> path = generatePathPoints(pathTail, nextCoordinate);
        result.addAll(path);
        return result;

    }

    /**
     * A search algorithm, don't understand it fully so matbe someone else? TODO: document because hard to understand
     *
     */
    public List<Coordinate> generatePathPoints(Coordinate startPosition, Coordinate endPosition) {
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
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        return gson.fromJson(graphString, Graph.class);
    }

    public List<Coordinate> getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(List<Coordinate> currentPath) {
        this.currentPath = currentPath;
    }
}