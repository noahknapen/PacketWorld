package util.graph;

/**
 *  A class representing a node state
 *  - FREE: Node is free
 *  - PACKET: Node is a packet node
 *  - DESTINATION: Node is a destination node
 */
public enum NodeType {
    FREE,
    PACKET,
    DESTINATION,
    BATTERYSTATION
}
