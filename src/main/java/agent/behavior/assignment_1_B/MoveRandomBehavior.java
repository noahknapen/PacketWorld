package agent.behavior.assignment_1_B;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import environment.Mail;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.Message;
import util.graph.AgentGraphInteraction;
import util.targets.BatteryStation;
import util.targets.Target;
import util.task.AgentTaskInteraction;

public class MoveRandomBehavior extends Behavior { 

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        Gson gson = new Gson();

        // Retrieve al the non-broadcasted battery Stations
        ArrayList<Target> nonBroadcastedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

        // If this list is bigger than zero broadcast the locations
        if (nonBroadcastedBatteryStations.size() > 0) {
            Message msg = new Message(gson.toJson(nonBroadcastedBatteryStations),"Battery");
            agentCommunication.broadcastMessage(gson.toJson(msg));
        }

        // Get all the messages from other agents
        ArrayList<Mail> messages = new ArrayList<>(agentCommunication.getMessages());

        // Iterate through all the messages
        for (int i=0; i < messages.size(); i++) {
            // Create a mail object to further inspect
            Mail mail = messages.get(i);
            Message msg = gson.fromJson(mail.getMessage(), new TypeToken<Message>(){}.getType());

            // If the type of the mail is Battery then we need to handle that kind of message
            if (Objects.equals(msg.getType(), "Battery")) handleBatteryMessages(agentState, msg);



            // Remove the message once it is processed
            agentCommunication.removeMessage(i);
        }

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, null, null, null, new ArrayList<>());
    }

    private void handleBatteryMessages(AgentState agentState, Message msg) {
        Gson gson = new Gson();

        // Retrieve all the already discovered battery stations
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);

        // Retrieve the list of batteryStations from the received message
        ArrayList<BatteryStation> newBatteryStations = gson.fromJson(msg.getMessage(), new TypeToken<ArrayList<BatteryStation>>(){}.getType());

        // Check if the battery station is already discovered else add it to the discovered
        for (BatteryStation batteryStation : newBatteryStations) {
            if (!discoveredBatteryStations.contains(batteryStation)) discoveredBatteryStations.add(batteryStation);
        }

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, null, null, discoveredBatteryStations, null);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        AgentGraphInteraction.handleGraph(agentState);

        // Check perception
        AgentGeneralNecessities.checkPerception(agentState);

        // Move randomly
        AgentGeneralNecessities.moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    } 
}