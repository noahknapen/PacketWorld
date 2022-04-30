package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import java.io.IOException;
import java.util.ArrayList;

public class MoveToChargingStationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        try {
            GeneralUtils.handleChargingStations(agentState, agentCommunication);
            GeneralUtils.handleDestinationLocations(agentState, agentCommunication);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        try {

            // Check the perception of the agent
            GeneralUtils.checkPerception(agentState);

            // Build the graph
            GraphUtils.build(agentState);

            // Move the agent to the target
            ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

            double minDistance = Double.MAX_VALUE;
            Coordinate bestPosition = null;
            ChargingStation bestStation = null;
            Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
            for (ChargingStation station : discoveredChargingStations) {

                // Find coordinates of charging station
                int batteryX = station.getCoordinate().getX();
                int batteryY = station.getCoordinate().getY() - 1;
                Coordinate chargingCoordinates = new Coordinate(batteryX, batteryY);

                // Guard clause to check if the charging station is closer than the previous one
                if (ActionUtils.calculateDistance(agentPosition, chargingCoordinates) > minDistance) continue;

                minDistance = ActionUtils.calculateDistance(agentPosition, chargingCoordinates);
                bestPosition = chargingCoordinates;
                bestStation = station;
            }

            // If the agent is close to the chargingStation, but it is in use skip a turn to conserve energy.
            if (bestPosition != null) {
                if (ActionUtils.calculateDistance(agentPosition, bestPosition) < 5 & bestStation.isInUse()) ActionUtils.skipTurn(agentAction);
                else ActionUtils.moveToPosition(agentState, agentAction, bestPosition);
            } else {
                ActionUtils.moveRandomly(agentState, agentAction);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}