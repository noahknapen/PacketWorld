package util.assignments.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import environment.Coordinate;
import util.assignments.jackson.NodeDeserializer;
import util.assignments.jackson.NodeSerializer;
import util.assignments.targets.Target;

/**
 * A class represening a graph
 */
public class Graph {

    // A data member holding the map of the graph
    @JsonSerialize(keyUsing = NodeSerializer.class)
    @JsonDeserialize(keyUsing = NodeDeserializer.class)
    private Map<Node, List<Node>> map;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Graph() {
        this.setMap(new HashMap<Node, List<Node>>());
    }

    @JsonCreator
    public Graph(@JsonProperty("map") Map<Node, List<Node>> map) {
        this.setMap(map);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Map<Node, List<Node>> getMap() {
        return map;
    }

    public void setMap(Map<Node, List<Node>> map) {
        this.map = map;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Get the corresponding node in the map based on the coordinate
     *
     * @param coordinate The coordinate of the possible node
     * @return The corresponding node or empty if no corresponding node was found
     */
    public Optional<Node> getNode(Coordinate coordinate) {
        return map.keySet().stream().filter(n -> n.getCoordinate().equals(coordinate)).findAny();
    }

    /**
     * Get all the targets of a specific type found in the graph
     *
     * @param <T> The type of the targets
     * @param targetClass The class of the targets
     * @return The list of targets of a specific type
     */
    @SuppressWarnings("unchecked")
    public <T extends Target> ArrayList<T> getTargets(Class<T> targetClass) {
        return new ArrayList<>(map.keySet().stream().filter(n -> (n.getTarget().isPresent() && n.getTarget().get().getClass().equals(targetClass))).map(n -> (T) n.getTarget().get()).collect(Collectors.toList()));
    }

    /**
     * Add a node to the graph
     * 
     * @param node The node
     */
    public void addNode(Node node) {
        // Check if the map contains the node
        if(map.containsKey(node)) {
            return;
        }
        
        // Add the node to the map
        map.put(node, new LinkedList<Node>());
    }

    /**
     * Add an edge between two nodes in the graph
     * 
     * @param node1 The first node
     * @param node2 The second node
     */
    public void addEdge(Node node1, Node node2) {
        // Check if the map does not contain the first node
        if(!map.containsKey(node1)) {
            return;
        }
        
        // Check if the map does not contain the second node
        if(!map.containsKey(node2)) {
            return;
        }

        // Check if no edge exists between the first node and the second node
        if(!map.get(node1).contains(node2)) {
            // Add an edge between the first node and the second node
            map.get(node1).add(node2);
        }

        // Check if no edge exists between the second node and the first node
        if(!map.get(node2).contains(node1)) {
            // Add an edge between the second node and the first node
            map.get(node2).add(node1);
        }
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
 
        for (Node node1 : map.keySet()) {
            builder.append("[" + node1.toString() + "]:\t");
            for (Node node2 : map.get(node1)) {
                builder.append("<" + node2.toString() + ">\t");
            }
            builder.append("\n");
        }
 
        return (builder.toString());
    }
}