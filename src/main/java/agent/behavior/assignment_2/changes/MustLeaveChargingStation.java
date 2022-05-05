package agent.behavior.assignment_2.changes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import org.checkerframework.checker.units.qual.A;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

public class MustLeaveChargingStation extends BehaviorChange {
    private boolean hasEnoughBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        // Retrieve agent state
        AgentState agentState = this.getAgentState();

        // Check whether the agent has enough battery
        boolean enoughBattery = determineIfAgentHasEnoughBattery(agentState);

        // Check whether an emergency notification was received
        boolean emergencyNotification = emergencyMessageReceived(agentState);

        // HasEnoughBattery is true if the battery level is high enough or if there is an emergency or both
        hasEnoughBattery = enoughBattery || emergencyNotification;

        // Reset memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EMERGENCY, false));

        // Guard clause to ensure that we are going to leave the station before updating the charging stations
        if (!hasEnoughBattery) return;

        // Update the charging station
        updateChargingStation(agentState);

    }

    @Override
    public boolean isSatisfied() {
        return hasEnoughBattery;
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Update the chargingStations with the right information. If the agent is standing in the charging station spot,
     * it will set the parameter of inUse to true and will update the optional battery level.
     *
     * @param agentState: The state of the agent.
     */
    private void updateChargingStation(AgentState agentState) {
        // Get agent position, + 1 because the spot to charge is one above the charging station
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY() + 1);
        
        // Get the current charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Iterate through all the stations to check them all
        for(ChargingStation chargingStation: chargingStations) {

            // A guard clause to ensure we are on the charging station
            if(!chargingStation.getCoordinate().equals(agentPosition)) continue;

            // Change the parameters
            chargingStation.setBatteryOfUser(Optional.empty());
            chargingStation.setInUse(false);

            // Set a variable in memory to true, this is for communication purposes
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, true));
        }

        // Update memory for charging stations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, chargingStations));
    }

    /**
     * Small helpfunction to determine whether the agent has enough battery or not.
     *
     * @param agentState: The state of the agent
     *
     * @return true if the agent has enough battery, false otherwise
     */
    private boolean determineIfAgentHasEnoughBattery(AgentState agentState) {
        return this.getAgentState().getBatteryState() >= 900;
    }

    /**
     * Small helpfunction to determine whether the agent has an emergency message in its memory or not
     *
     * @param agentState: The state of the agent
     *
     * @return true if the agent has an emergency message, false otherwise
     */
    private boolean emergencyMessageReceived(AgentState agentState) {
        return Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.EMERGENCY, Boolean.class));
    }
}
