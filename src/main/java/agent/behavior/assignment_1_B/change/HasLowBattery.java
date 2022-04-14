package agent.behavior.assignment_1_B.change;

import agent.AgentAction;
import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.targets.Target;

import java.util.ArrayList;

public class HasLowBattery extends BehaviorChange {
    private boolean hasLowBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> usedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.USED_BATTERY_STATIONS);

        for (Target station : discoveredBatteryStations) {
            System.out.printf("Station:%s for agent:%s\n", station.getCoordinate() ,agentState.getName() );

            if (!usedBatteryStations.contains(station)) {
                hasLowBattery = agentState.getBatteryState() < 500;
            } else {
                hasLowBattery = false;
            }
        }
    }

    @Override
    public boolean isSatisfied() {
        return hasLowBattery;
    }

}
