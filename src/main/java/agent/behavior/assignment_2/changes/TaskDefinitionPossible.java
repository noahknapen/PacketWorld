package agent.behavior.assignment_2.changes;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import com.google.common.base.Optional;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.comparators.PacketComparator;
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
        taskDefinitionPossible = checkTaskDefinition(agentState);  
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
     */
    private boolean checkTaskDefinition(AgentState agentState) {
        // Get the discovered packets and discovered destinations
        ArrayList<Packet> discoveredPackets = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_PACKETS, Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_DESTINATIONS, Destination.class);

        // Sort the discovered packets
        PacketComparator packetComparator = new PacketComparator(agentState, discoveredDestinations);
        Collections.sort(discoveredPackets, packetComparator);

        // Loop over the sorted discovered packets
        for(int i = 0; i < discoveredPackets.size(); i++) {
            // Get a candidate packet
            Packet candidatePacket= discoveredPackets.get(i);

            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();
            
            // Loop over the discovered destinations
            for(int j = 0; j < discoveredDestinations.size(); j++) {
                // Get a candidate destination
                Destination candidateDestination = discoveredDestinations.get(j);

                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();
                
                // Check if the colors correspond
                if(candidatePacketColor.equals(candidateDestinationColor)) {
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
}
