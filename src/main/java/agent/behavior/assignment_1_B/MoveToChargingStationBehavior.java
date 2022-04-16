package agent.behavior.assignment_1_B;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.AgentComNecessities;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.targets.Target;

import java.util.ArrayList;

public class MoveToChargingStationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        AgentComNecessities.handleBatteryStations(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        AgentGraphInteraction.handleGraph(agentState);

        // Check perception
        AgentGeneralNecessities.checkPerception(agentState);

        // Move to battery station
        // Retrieve the list of all discoveredBatteryStations
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> usedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.USED_BATTERY_STATIONS);

        for (Target station : discoveredBatteryStations) {

            if (usedBatteryStations.contains(station)) continue;

            // Find coordinates of charging station
            int batteryX = station.getCoordinate().getX();
            int batteryY = station.getCoordinate().getY() - 1;
            Coordinate chargingCoordinates = new Coordinate(batteryX, batteryY);

            // Move to the battery station
            AgentGeneralNecessities.moveToPosition(agentState, agentAction, chargingCoordinates);
            AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null, null);
            return;
        }

        AgentGeneralNecessities.moveRandom(agentState, agentAction);
        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null, null);
    }
}
