package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.comparators.PacketComparator;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;
import util.assignments.task.TaskType;

/**
 * A behavior change class that checks if a new task can be defined
 */
public class TaskDefinitionPossible extends BehaviorChange{

    private boolean taskDefinitionPossible = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Handle the possible task definition
        try {
            taskDefinitionPossible = checkTaskDefinition(agentState);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isSatisfied() {
        return taskDefinitionPossible;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check if a task can be defined and do so if yes
     *
     * @param agentState The current state of the agent
     * @return True if the task defintion is possible and was done, otherwise false
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private boolean checkTaskDefinition(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get the discovered packets and discovered destinations
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Sort the discovered packets
        PacketComparator packetComparator = new PacketComparator(agentState, discoveredDestinations);
        discoveredPackets.sort(packetComparator);

        // Loop over the sorted discovered packets
        for(int i = 0; i < discoveredPackets.size(); i++) {
            // Get a candidate packet
            Packet candidatePacket= discoveredPackets.get(i);

            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {
                // Get a candidate destination
                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (candidatePacketColor.equals(candidateDestinationColor)) {
                    if (!hasEnoughBatteryToCompleteTask(agentState, candidatePacket, candidateDestination)) continue;

                    // Remove the packet from the discovered packets
                    candidatePacket = discoveredPackets.remove(i);

                    // Define the task
                    Task task = new Task(TaskType.MOVE_TO_PACKET, Optional.of(candidatePacket), Optional.of(candidateDestination));

                    // Update the memory
                    MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task, MemoryKeys.DISCOVERED_PACKETS, discoveredPackets));

                    return true;
                }
            }
        }

        return false;
    }

    private boolean hasEnoughBatteryToCompleteTask(AgentState agentState, Packet packet, Destination destination) {
        // First calculate the power to go to the packet location
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        Coordinate packetPosition = packet.getCoordinate();
        double powerToGoToPacket = GeneralUtils.calculateEuclideanDistance(agentPosition, packetPosition) * 10;

        // Second calculate the power to go from packet to destination
        Coordinate destinationPosition = destination.getCoordinate();
        double powerToGoToDestination = GeneralUtils.calculateEuclideanDistance(packetPosition, destinationPosition) * 25;

        return agentState.getBatteryState() > (powerToGoToDestination + powerToGoToPacket + 150);
    }
}