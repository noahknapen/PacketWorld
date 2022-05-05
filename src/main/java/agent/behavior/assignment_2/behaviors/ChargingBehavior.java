package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.util.ArrayList;
import util.assignments.targets.ChargingStation;

import java.util.Map;
import java.util.Optional;

public class ChargingBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Update the current charging stations in memory
        updateChargingStation(agentState);

        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Build the graph
        GraphUtils.build(agentState);

        // Charging is staying still on charging spot -> skip a turn
        agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Update the chargingStations with the right information. If the agent is standing in the charging station spot,
     * it will set the parameter of inUse to true and will update the optional battery level.
     *
     * @param agentState: The state of the agent.
     */
    private void updateChargingStation(AgentState agentState) {
        // Get agent position, + 1 because the spot to charge is one above the charging station
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY() + 1);
        
        // Get the current charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Iterate through all the stations to check them all
        for(ChargingStation chargingStation: chargingStations) {

            // A guard clause to ensure we are on the charging station
            if(!chargingStation.getCoordinate().equals(agentPosition)) continue;

            // Change the parameters
            chargingStation.setBatteryOfUser(Optional.of(agentState.getBatteryState()));
            chargingStation.setInUse(true);

            // Set a variable in memory to true, this is for communication purposes
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, true));

        }

        // Update memory for charging stations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, chargingStations));
    }
}