package agent.behavior.assignment_1_B.change;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import agent.behavior.assignment_1_B.MoveToChargingStationBehavior;
import environment.Coordinate;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.targets.Target;

import java.util.ArrayList;
import java.util.Objects;

public class IsOnChargingPad extends BehaviorChange {
    private boolean isOnChargingPad = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();

        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);

        for (Target station : discoveredBatteryStations) {

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

    }

    @Override
    public boolean isSatisfied() {
        return isOnChargingPad;
    }

}
