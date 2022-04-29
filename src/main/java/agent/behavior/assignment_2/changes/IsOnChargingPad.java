package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class IsOnChargingPad extends BehaviorChange {
    private boolean isOnChargingPad = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        try {
            AgentState agentState = this.getAgentState();

            int agentX = agentState.getX();
            int agentY = agentState.getY();
            Coordinate agentPosition = new Coordinate(agentX, agentY);

            ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

            for (ChargingStation station : discoveredChargingStations) {

                int stationX = station.getCoordinate().getX();
                int stationY = station.getCoordinate().getY() - 1;
                Coordinate stationPosition = new Coordinate(stationX, stationY);

                if (stationPosition.equals(agentPosition)) {
                    isOnChargingPad = true;
                    break;
                } else {
                    isOnChargingPad = false;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public boolean isSatisfied() {
        return isOnChargingPad;
    }

}