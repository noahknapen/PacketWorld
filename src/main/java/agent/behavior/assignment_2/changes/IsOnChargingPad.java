package agent.behavior.assignment_2.changes;

import java.util.ArrayList;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;


public class IsOnChargingPad extends BehaviorChange {
    private boolean isOnChargingPad = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        // Check if the agent is on a charging pad
        isOnChargingPad = handleIsOnChargingPad(agentState);
    }

    @Override
    public boolean isSatisfied() {
        return isOnChargingPad;
    }


    /////////////
    // METHODS //
    /////////////

    /**
     * A function that is used to figure out if the agent is on the charging station.
     *
     * @param agentState The current state of the agent
     * @return True if the agent is an the charging station, false otherwise.
     */
    public static boolean handleIsOnChargingPad(AgentState agentState) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Get the charging stations from memory
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Iterate through all stations
        for (ChargingStation discoveredChargingStation : discoveredChargingStations) {
            // Get the position of the charging station
            int discoveredChargingStationX = discoveredChargingStation.getCoordinate().getX();
            int discoveredChargingStationY = discoveredChargingStation.getCoordinate().getY() - 1;
            Coordinate discoveredChargingStationPosition = new Coordinate(discoveredChargingStationX, discoveredChargingStationY);

            // Check if the positions correspond
            if (discoveredChargingStationPosition.equals(agentPosition)) return true;
        }

        // If the loop finished, the agent isn't on a charging station
        return false;
    }
}