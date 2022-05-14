package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import java.util.ArrayList;


public class MustGoToChargingStation extends BehaviorChange {
    private boolean mustGoCharge = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        /*
        What do we want to take into account when deciding to go to a station
        - Will the station be empty when we arrive?
        - Do we have enough battery to continue working or must we go straight to there?
         */
        // Get the state of the agent
        AgentState agentState = this.getAgentState();

        // Retrieve the charging stations from memory
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Iterate through all the stations
        for (ChargingStation station : discoveredChargingStations) {

            // A guard clause to check if we have enough battery to get to the station
            if (enoughBatteryToContinueWorking(station)) {
                mustGoCharge = false;
                continue;
            }

/*            // A guard clause to check if the station is empty or will be empty when the agent arrives
            if (station.isInUse() && !emptyWhenWeArrive(station)) {
                mustGoCharge = false;
                continue;
            }

 */
            mustGoCharge = true;
            break;
        }
    }

    @Override
    public boolean isSatisfied() {
        return mustGoCharge;
    }

    /**
     * A function that determines whether the agent has enough battery to come to the station.
     *
     * @param station: The charging station
     *
     * @return True if the agents has enough battery to go to the station, false otherwise.
     */
    private boolean enoughBatteryToContinueWorking(ChargingStation station) {
        // Calculate the turns away from the station
        Coordinate agentPosition = new Coordinate(this.getAgentState().getX(), this.getAgentState().getY());
        Coordinate stationPosition = station.getCoordinate();
        double turnsAwayFromStation = Perception.distance(agentPosition.getX(), agentPosition.getY(), stationPosition.getX(), stationPosition.getY());

        // Calculate the energy it would cost to go to the station
        double energyUsedToGetToStation = turnsAwayFromStation * GeneralUtils.COST_WALK_WITHOUT_PACKET;

        // Derive the energy surplus of the agent
        double energySurplus = getAgentState().getBatteryState() - energyUsedToGetToStation;

        // Take a small buffer of eight turns into account
        return energySurplus > 80;
    }
}