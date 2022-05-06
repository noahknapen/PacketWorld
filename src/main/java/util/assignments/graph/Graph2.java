package util.assignments.graph;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import util.assignments.jackson.NodeDeserializer;
import util.assignments.jackson.NodeSerializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Graph2{
    private final Set<Node> nodes;
    private final Map<Node, Set<Node>> connections;

    public Node getNode(Node nodeToGet) {
        return nodes.stream()
                .filter(node -> node.equals(nodeToGet))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No node found with ID"));
    }

    public Set<Node> getConnections(Node node) {
        return connections.get(node).stream()
                .map(this::getNode)
                .collect(Collectors.toSet());
    }

    public Graph2() {
        this.nodes = new HashSet<>();
        this.connections = new HashMap<>();
    }

    public void addNode(Node node) {
        if (nodes.contains(node)) return;

        nodes.add(node);
    }

    public void removeNode(Node node) {
        if (!nodes.contains(node)) return;

        nodes.remove(node);
    }

    public void addEdge(Node node1, Node node2) {
        if(!nodes.contains(node1)) addNode(node1);
        if(!nodes.contains(node2)) addNode(node2);

        Set<Node> connectionsOfNode1 = getConnections(node1);
        Set<Node> connectionsOfNode2 = getConnections(node2);

        connectionsOfNode1.add(node2);
        connectionsOfNode2.add(node1);

        connections.put(node1, connectionsOfNode1);
        connections.put(node2, connectionsOfNode2);
    }

    public void removeEdge(Node node1, Node node2) {
        if(!nodes.contains(node1)) addNode(node1);
        if(!nodes.contains(node2)) addNode(node2);

        Set<Node> connectionsOfNode1 = getConnections(node1);
        Set<Node> connectionsOfNode2 = getConnections(node2);

        connectionsOfNode1.remove(node2);
        connectionsOfNode2.remove(node1);

        connections.put(node1, connectionsOfNode1);
        connections.put(node2, connectionsOfNode2);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();

        for(Node node : nodes) {
            builder.append(node.toString()).append(": ");
            for(Node neighbour : getConnections(node)) {
                builder.append(neighbour.toString()).append(" ");
            }
            builder.append("\n");
        }

        return (builder.toString());
    }
}
