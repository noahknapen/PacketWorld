package util.assignments.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @JsonProperty("map")
    private Map<Node, List<Node>> map;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Graph() {
        this.setMap(new HashMap<Node, List<Node>>());
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

    /**
     * Finds the corresponding node in the map based on the coordinate
     *
     * @param coordinate The coordinate of the node
     * @return The node if there exists one, otherwise empty
     */
    public Optional<Node> getNode(Coordinate coordinate) {
        return map.keySet().stream().filter(n -> n.getCoordinate().equals(coordinate)).findAny();
    }

    /**
     * Get all the targets of a specific type found in the graph
     *
     * @param <T> The type of the targets
     * @param targetClass The class of the targets
     * @return An arraylist of all the targets of a specific type found in the graph
     */
    public <T extends Target> ArrayList<T> getTargets(Class<T> targetClass) {
        return new ArrayList<>(map.keySet().stream().filter(n -> (n.getTarget().isPresent() && n.getTarget().get().getClass().equals(targetClass))).map(n -> (T) n.getTarget().get()).collect(Collectors.toList()));
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Add a node to the map
     * 
     * @param node The node to add
     */
    public void addNode(Node node) {
        // If node exists
        if(map.containsKey(node)) return;
        
        // Add the node
        map.put(node, new LinkedList<Node>());
    }

    public void addEdge(Node node1, Node node2) {
        // Check if the map does not contain node 1 and add it if so
        if(!map.containsKey(node1))
            addNode(node1);
        
        // Check if the map does not contain node 2 and add it if so
        if(!map.containsKey(node2))
            addNode(node2);

        // Check if the edges do not exist yet and add them if so
        if(!map.get(node1).contains(node2))
            map.get(node1).add(node2);
        if(!map.get(node2).contains(node1))
            map.get(node2).add(node1);
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
 
        for (Node node1 : map.keySet()) {
            builder.append(node1.toString() + ": ");
            for (Node node2 : map.get(node1)) {
                builder.append(node2.toString() + " ");
            }
            builder.append("\n");
        }
 
        return (builder.toString());
    }
}
