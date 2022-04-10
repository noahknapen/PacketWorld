package util.targets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

import agent.AgentState;

/**
 * A class implementing a comparator for packets
 * It is used to sort packets that have to be delivered
 * Packets are sorted based on:
 * - If a corresponding (color) destination is already discovered
 * - Position between packet and agent: Packets closer to current position of agent are picked up first
 */
public class PacketComparator implements Comparator<Packet> {

    private int agentX;
    private int agentY;
    private ArrayList<Color> discoveredDestinationColors;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public PacketComparator(AgentState agentState, ArrayList<Destination> discoveredDestinations) {
        this.agentX = agentState.getX();
        this.agentY = agentState.getY();
        this.discoveredDestinationColors = (ArrayList<Color>) discoveredDestinations.stream().map(Destination::getColor).collect(Collectors.toList());
    }

    //////////////
    // OVERRIDES//
    //////////////
    
    /**
     * Compare two packets
     * First, compare based on the discovered color
     * Second, compare based on distance between the packet and the agent
     */
    @Override
    public int compare(Packet packet1, Packet packet2) {
        Color packet1Color = packet1.getColor();
        Color packet2Color = packet2.getColor();
        boolean packet1ColorDiscovered = discoveredDestinationColors.contains(packet1Color);
        boolean packet2ColorDiscovered = discoveredDestinationColors.contains(packet2Color);

        if(!packet1ColorDiscovered && !packet2ColorDiscovered) return 0;
        else if(packet1ColorDiscovered && !packet2ColorDiscovered) return -1;
        else if(!packet1ColorDiscovered && packet2ColorDiscovered) return 1;
        else {
            int packet1X = packet1.getCoordinate().getX();
            int packet1Y = packet1.getCoordinate().getY();
            int packet1Dx = agentX - packet1X;
            int packet1Dy = agentY - packet1Y;
            double distancePacket1 = Math.sqrt(((packet1Dx * packet1Dx) + (packet1Dy * packet1Dy)));
    
            int packet2X = packet2.getCoordinate().getX();
            int packet2Y = packet2.getCoordinate().getY();
            int packet2Dx = agentX - packet2X;
            int packet2Dy = agentY - packet2Y;
            double distancePacket2 = Math.sqrt(((packet2Dx * packet2Dx) + (packet2Dy * packet2Dy)));

            if(distancePacket1 == distancePacket2) return 0;
            else if(distancePacket1 < distancePacket2) return -1;
            else return 1;
        }       
        
    }
    
}
