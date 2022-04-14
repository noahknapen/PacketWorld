package agent.behavior.assignment_1_B;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import environment.Coordinate;
import environment.Mail;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.targets.BatteryStation;
import util.targets.Target;
import util.task.AgentTaskInteraction;
import util.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

public class MoveToChargingStationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        Gson gson = new Gson();

        // Broadcast found destinations to other agents
        ArrayList<Target> nonBroadcastedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

        if (nonBroadcastedBatteryStations.size() > 0) {
            String batteryStationsString = gson.toJson(nonBroadcastedBatteryStations);
            agentCommunication.broadcastMessage(batteryStationsString);
            System.out.printf("Agent on coordinate (%d,%d) has broadcasted a message%n", agentState.getX(), agentState.getY());
        }

        // Get messages from other agents
        Collection<Mail> messages = agentCommunication.getMessages();
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);

        for (Mail message : messages) {
            System.out.printf("Agent on coordinate (%d,%d) has received a message%n", agentState.getX(), agentState.getY());
            ArrayList<BatteryStation> newBatteryStations = gson.fromJson(message.getMessage(), new TypeToken<ArrayList<BatteryStation>>(){}.getType());

            for (BatteryStation batteryStation : newBatteryStations)
            {
                if (!discoveredBatteryStations.contains(batteryStation))
                {
                    discoveredBatteryStations.add(batteryStation);
                }
            }
        }

        AgentTaskInteraction.updateTaskMemory(agentState, null, null, discoveredBatteryStations, new ArrayList<>());
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

        // Move to battery station
        // Retrieve the list of all discoveredBatteryStations
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> usedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.USED_BATTERY_STATIONS);

        for (Target station : discoveredBatteryStations) {
            System.out.printf("Station:%s for agent:%s\n", station.getCoordinate() ,agentState.getName() );

            if (usedBatteryStations.contains(station)) continue;

            // Find coordinates of charging station
            int batteryX = station.getCoordinate().getX();
            int batteryY = station.getCoordinate().getY() - 1;
            Coordinate chargingCoordinates = new Coordinate(batteryX, batteryY);

            // Move to the battery station
            AgentGeneralNecessities.moveToPosition(agentState, agentAction, chargingCoordinates);
            AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
            return;
        }

        AgentGeneralNecessities.moveRandom(agentState, agentAction);
        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }
}
