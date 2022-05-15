package util.assignments.graph;

import java.awt.*;
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
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
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

    @JsonProperty("packetNodes")
    private ArrayList<Node> packetNodes;

    @JsonProperty("destinationNodes")
    private ArrayList<Node> destinationNodes;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Graph() {
        this.setMap(new HashMap<Node, List<Node>>());
        this.setPacketNodes(new ArrayList<>());
        this.setDestinationNodes(new ArrayList<>());
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
     * @return An arraylist of all the targets of a specific type found in the graph
     */
    public ArrayList<Packet> getPackets() {
        return new ArrayList<Packet>(getPacketNodes().stream().map(n -> (Packet) n.getTarget().get()).collect(Collectors.toList()));
    }

    /**
     * Get all the targets of a specific type found in the graph
     *
     * @return An arraylist of all the targets of a specific type found in the graph
     */
    public ArrayList<Destination> getDestinations() {
        return new ArrayList<Destination>(getDestinationNodes().stream().map(n -> (Destination) n.getTarget().get()).collect(Collectors.toList()));
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

    public ArrayList<Node> getPacketNodes() {
        return packetNodes;
    }

    public void setPacketNodes(ArrayList<Node> packetNodes) {
        this.packetNodes = packetNodes;
    }

    public ArrayList<Node> getDestinationNodes() {
        return destinationNodes;
    }

    public void setDestinationNodes(ArrayList<Node> destinationNodes) {
        this.destinationNodes = destinationNodes;
    }


    public void updateTargetLists(Optional<Node> node, Optional<Color> agentColor) {
        // Remove targets from lists if free
        if (!node.get().containsTarget() && getPacketNodes().contains(node.get())) {
            this.packetNodes.remove(node.get());
        }

        else if (!node.get().containsTarget() && getDestinationNodes().contains(node.get())) {
            this.packetNodes.remove(node.get());
        }

        // Add to lists if contains target
        else if (node.get().containsPacket() && !getPacketNodes().contains(node.get())) {
            Packet packet = (Packet) node.get().getTarget().get();
            if (agentColor.isEmpty() || (agentColor.get().getRGB() == packet.getRgbColor())) {
                this.packetNodes.add(node.get());
            }
        }

        else if (node.get().containsDestination() && !getDestinationNodes().contains(node.get())) {
            Destination destination = (Destination) node.get().getTarget().get();
            if (agentColor.isEmpty() || (agentColor.get().getRGB() == destination.getRgbColor())) {
                this.destinationNodes.add(node.get());
            }
        }

    }
}
