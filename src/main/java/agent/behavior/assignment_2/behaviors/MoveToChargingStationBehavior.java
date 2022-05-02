package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.ActionUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import javax.management.MBeanException;
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

                // Calculate the distance between current position and the current charging station
                int currentDistance = Perception.distance(agentPosition.getX(), agentPosition.getY(), chargingCoordinates.getX(), chargingCoordinates.getY());

                // Guard clause to check if the charging station is closer than the previous one
                if (currentDistance > minDistance) continue;

                // Set the variables to the new best option
                minDistance = currentDistance;
                bestPosition = chargingCoordinates;
                bestStation = station;
            }

            // If the agent is close to the chargingStation, but it is in use skip a turn to conserve energy.
            if (bestPosition != null) {
                int distance = Perception.distance(agentPosition.getX(), agentPosition.getY(), bestPosition.getX(), bestPosition.getY());
                if (distance < 5 & bestStation.isInUse()) ActionUtils.skipTurn(agentAction);
                else ActionUtils.moveToPosition(agentState, agentAction, bestPosition);
            } else {
                ActionUtils.moveRandomly(agentState, agentAction);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}