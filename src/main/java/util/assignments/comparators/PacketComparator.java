package util.assignments.comparators;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import agent.AgentState;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

/**
 * A class implementing a comparator for packets
 * It is used to sort packets that have to be delivered
 * Packets are sorted based on:
 * - If theey are priority packets
 * - If a corresponding (color) destination is already discovered
 * - Position between packet and agent: Packets closer to current position of agent are picked up first
 */
public class PacketComparator implements Comparator<Packet> {

    private AgentState agentState;
    private ArrayList<Color> discoveredDestinationColors;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public PacketComparator(AgentState agentState, ArrayList<Destination> discoveredDestinations) {
        this.setAgentState(agentState);
        this.setDiscoveredDestinationColors((ArrayList<Color>) discoveredDestinations.stream().map(Destination::getColor).collect(Collectors.toList()));
    }

    /////////////
    // SETTERS //
    /////////////

    private void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }
    
    private void setDiscoveredDestinationColors(ArrayList<Color> discoveredDestinationColors) {
        this.discoveredDestinationColors = discoveredDestinationColors;
    }

    //////////////
    // OVERRIDES//
    //////////////
    
    /**
     * Compare two packets
     * First, compare based on priority.
     * Second, compare based on the discovered color.
     * Third, compare based on distance between the packet and the agent.
     * 
     * @param packet1 The first packet
     * @param packet2 The second packet
     * @return 0 if both packets are equivalent, 1 if the first packet has priority, -1 if the second packet has priority
     */
    @Override
    public int compare(Packet packet1, Packet packet2) {
        // Get the coordinate of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentCoordinate = new Coordinate(agentX, agentY);

        // Compare based on priority
        if (packet1.hasPriority()) return 1;
        if (packet2.hasPriority()) return -1;

        // Get the colors of both packets
        Color packet1Color = packet1.getColor();
        Color packet2Color = packet2.getColor();
        
        // Get if the color was discovered for both packets
        boolean packet1ColorDiscovered = discoveredDestinationColors.contains(packet1Color);
        boolean packet2ColorDiscovered = discoveredDestinationColors.contains(packet2Color);

        // Compare based on discovered color
        if(!packet1ColorDiscovered && !packet2ColorDiscovered) return 0;
        else if(packet1ColorDiscovered && !packet2ColorDiscovered) return -1;
        else if(!packet1ColorDiscovered && packet2ColorDiscovered) return 1;
        else {
            double distancePacket1 = GeneralUtils.calculateEuclideanDistance(packet1.getCoordinate(), agentCoordinate);
            double distancePacket2 = GeneralUtils.calculateEuclideanDistance(packet2.getCoordinate(), agentCoordinate);
            
            // Compare based on distance between the packet and the agent
            if(distancePacket1 == distancePacket2) return 0;
            else if(distancePacket1 < distancePacket2) return -1;
            else return 1;
        }
    }
}
