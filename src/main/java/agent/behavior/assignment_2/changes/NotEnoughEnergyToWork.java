package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import util.assignments.comparators.PacketComparator;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.Graph;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

import java.awt.*;
import java.util.ArrayList;

public class NotEnoughEnergyToWork extends BehaviorChange {

    private boolean notEnoughEnergyToWork = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the agent has enough energy to work
        notEnoughEnergyToWork = isNotEnoughEnergyToWork(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return notEnoughEnergyToWork;
    }

    /**
     * A function used to determine whether an agent should continue working or if they should move randomly,
     * explore, or go to the charging station.
     *
     * @param agentState: The state of the agent
     *
     * @return True if the agent can continue to work, false otherwise
     */
    private boolean isNotEnoughEnergyToWork(AgentState agentState) {
        // Get the discovered packets and discovered destinations
        ArrayList<Packet> discoveredPackets = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Packet.class);
        ArrayList<Destination> discoveredDestinations = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Destination.class);

        // Sort the discovered packets
        PacketComparator packetComparator = new PacketComparator(agentState, discoveredDestinations);
        discoveredPackets.sort(packetComparator);

        // Loop over the sorted discovered packets
        for (Packet candidatePacket : discoveredPackets) {

            // Get the color of the candidate packet
            Color candidatePacketColor = candidatePacket.getColor();

            // Loop over the discovered destinations
            for (Destination candidateDestination : discoveredDestinations) {

                // Get the color of the candidate destination
                Color candidateDestinationColor = candidateDestination.getColor();

                // Check if the colors correspond
                if (!candidatePacketColor.equals(candidateDestinationColor)) continue;

                // Check if the agent has enough energy left
                if (GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, candidatePacket, candidateDestination)) return false;

            }
        }

        return true;
    }

}
