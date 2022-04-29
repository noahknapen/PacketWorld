package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
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

            for (ChargingStation station : discoveredChargingStations) {

                // Find coordinates of charging station
                int batteryX = station.getCoordinate().getX();
                int batteryY = station.getCoordinate().getY() - 1;
                Coordinate chargingCoordinates = new Coordinate(batteryX, batteryY);

                // Move to the battery station
                ActionUtils.moveToPosition(agentState, agentAction, chargingCoordinates);
                return;
            }

            ActionUtils.moveRandomly(agentState, agentAction);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}