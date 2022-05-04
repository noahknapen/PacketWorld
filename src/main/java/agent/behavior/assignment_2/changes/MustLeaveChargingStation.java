package agent.behavior.assignment_2.changes;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;


import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
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

        // If the agent has enough battery
        hasEnoughBattery =  this.getAgentState().getBatteryState() >= 900;

        // Guard clause to ensure the agent has enough battery before updating the station.
        if(!hasEnoughBattery) return;

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
}
