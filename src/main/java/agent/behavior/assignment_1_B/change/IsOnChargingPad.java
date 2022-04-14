package agent.behavior.assignment_1_B.change;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.targets.Target;

import java.util.ArrayList;

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
            System.out.printf("Coordinates charging station: %s, coordinates agent: %s %s\n", station.getCoordinate(), agentState.getName(), agentPosition);

            int stationX = station.getCoordinate().getX();
            int stationY = station.getCoordinate().getY() - 1;
            Coordinate stationPosition = new Coordinate(stationX, stationY);

            if (stationPosition.equals(agentPosition) && agentState.getBatteryState() < 750) {
                System.out.println("On chargingpad");
                isOnChargingPad = true;
                break;
            }
        }

    }

    @Override
    public boolean isSatisfied() {
        return isOnChargingPad;
    }

}
