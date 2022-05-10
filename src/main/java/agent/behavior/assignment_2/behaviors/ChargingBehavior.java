package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.CommunicationUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.util.ArrayList;
import util.assignments.targets.ChargingStation;

import java.util.HashMap;
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
        // GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);

        // Check for emergency notifications
        checkForEmergencyNotifications(agentState, agentCommunication);

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
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

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
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.CHARGING_STATIONS, chargingStations));
    }

    /**
     * A function that checks if there are any emergency messages in the mailbox, and if so it will change the
     * emergency in the memory.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     */
    private void checkForEmergencyNotifications(AgentState agentState, AgentCommunication agentCommunication) {
        // Ensure that there are messages before continuing
        if (agentCommunication.getNbMessages() == 0) return;

        // Retrieve the messages
        HashMap<String, Boolean> receivedMessage = CommunicationUtils.getObjectsFromMails(agentCommunication, "boolean", Boolean.class);

        // No messages received so no emergency
        if (receivedMessage == null) return;

        // If the emergency camo from us, skip it
        if (receivedMessage.containsKey(agentState.getName())) return;

        for (String sender : receivedMessage.keySet()) {
            boolean emergency = receivedMessage.get(sender);
            // If no emergency skip
            if (!(emergency)) continue;

            // Change the memory due to the emergency
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.EMERGENCY, true));
        }
    }
}