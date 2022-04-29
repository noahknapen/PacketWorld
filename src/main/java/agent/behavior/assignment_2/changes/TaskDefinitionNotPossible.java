package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.comparators.PacketComparator;
import util.assignments.general.ActionUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior change class that checks if a new task cannot be defined
 */
public class TaskDefinitionNotPossible extends BehaviorChange{

    private boolean taskDefinitionNotPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the task definition is not possible
        try {
            taskDefinitionNotPossible = checkNoTaskDefinition(agentState);
        } catch (IOException e) {
            e.printStackTrace();
        }    
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionNotPossible;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if no task can be defined
     * 
     * @param agentState The current state of the agent
     * @return True if the task defintion is not possible, otherwise false
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private boolean checkNoTaskDefinition(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get the discovered packets
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);

        // Check if no packets were discovered yet and return true if so
        if(discoveredPackets.isEmpty()) return true;

        // Get the discovered destinations
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Check if no destinations were discovered yet and return true if so
        if(discoveredDestinations.isEmpty()) return true;

        // Sort the discovered packets
        PacketComparator packetComparator = new PacketComparator(agentState, discoveredDestinations);
        discoveredPackets.sort(packetComparator);

        // Loop over the sorted discovered packets
        for (Packet candidatePacket : discoveredPackets) {
            // Get a candidate packet
            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {
                // Get a candidate destination
                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (!candidatePacketColor.equals(candidateDestinationColor)) continue;

                // If the agent doesn't have enough energy to complete the task, don't do the task
                if (!(determineEnergyNecessaryToPickAndDeliverPacket(agentState, candidatePacket, candidateDestination) < agentState.getBatteryState() + 150)) return true;
            }
        }

        // Transform lists of discovered targets to lists of discovered targets' color
        ArrayList<Color> discoveredPacketColors = (ArrayList<Color>) discoveredPackets.stream().map(Packet::getColor).collect(Collectors.toList());
        ArrayList<Color> discoveredDestinationColors = (ArrayList<Color>) discoveredDestinations.stream().map(Destination::getColor).collect(Collectors.toList());
       
        // Check if there are no corresponding (color) destinations for the packets in the discovered packets list and return true if so
        if(discoveredPacketColors.stream().noneMatch(e -> discoveredDestinationColors.contains(e))) return true;

        return false;
    }

    private double determineEnergyNecessaryToPickAndDeliverPacket(AgentState agentState, Packet packet, Destination destination) {
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        double distance1 = ActionUtils.calculateDistance(agentPosition, packet.getCoordinate());
        double power1 = Math.ceil(distance1*10);


        double distance2 = ActionUtils.calculateDistance(packet.getCoordinate(), destination.getCoordinate());
        double power2 = Math.ceil(distance2*25);

        return power1 + power2;
    }
}
