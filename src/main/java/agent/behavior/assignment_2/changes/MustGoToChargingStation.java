package agent.behavior.assignment_2.changes;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


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
        - The closest station
         */
        try {
            AgentState agentState = this.getAgentState();
            ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

            for (ChargingStation station : discoveredChargingStations) {

                // A guard clause to check if we have enough battery to get to the station
                if (enoughBatteryToContinueWorking(station)) {
                    mustGoCharge = false;
                    continue;
                }

                // A guard clause to check if the station is empty or will be empty when the agent arrives
                if (station.isInUse() && !emptyWhenWeArrive(station)) {
                    mustGoCharge = false;
                    continue;
                }

                mustGoCharge = true;
                break;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean emptyWhenWeArrive(ChargingStation station) {
        Optional<Integer> batteryLevelUserOnStation = station.getBatteryOfUser();

        // If the station isn't in use it is currently projected free for when we arrive.
        if (!station.isInUse() || batteryLevelUserOnStation.isEmpty()) return true;

        // Calculate the turns away from the station and the turns the station is in use
        Coordinate agentPosition = new Coordinate(this.getAgentState().getX(), this.getAgentState().getY());
        Coordinate stationPosition = station.getCoordinate();
        double turnsAwayFromStation = Perception.distance(agentPosition.getX(), agentPosition.getY(), stationPosition.getX(), stationPosition.getY());
        double turnsTheStationIsInUse = (environment.EnergyValues.BATTERY_MAX - batteryLevelUserOnStation.get()) / 100.0;

        // If the turns away from the station is more than the turns in use, the station will be empty upon arrival
        return Math.ceil(turnsAwayFromStation) > Math.ceil(turnsTheStationIsInUse) + 5;
    }

    private boolean enoughBatteryToContinueWorking(ChargingStation station) {
        // Calculate the turns away from the station
        Coordinate agentPosition = new Coordinate(this.getAgentState().getX(), this.getAgentState().getY());
        Coordinate stationPosition = station.getCoordinate();
        double turnsAwayFromStation = Perception.distance(agentPosition.getX(), agentPosition.getY(), stationPosition.getX(), stationPosition.getY());

        // Calculate the energy it would cost to go to the station
        double energyUsedToGetToStation = turnsAwayFromStation * GeneralUtils.WALK_WITHOUT_PACKET;

        // Derive the energy surplus of the agent
        double energySurplus = getAgentState().getBatteryState() - energyUsedToGetToStation;

        // Take a small buffer of ten turns into account
        return energySurplus > 100;
    }

    @Override
    public boolean isSatisfied() {
        return mustGoCharge;
    }

}