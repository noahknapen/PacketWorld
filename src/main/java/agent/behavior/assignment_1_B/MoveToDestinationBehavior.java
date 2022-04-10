package agent.behavior.assignment_1_B;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

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
import util.graph.AgentGraphInteraction;
import util.targets.BatteryStation;
import util.targets.Target;
import util.task.AgentTaskInteraction;
import util.task.Task;

public class MoveToDestinationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        Gson gson = new Gson();

        // Broadcast found destinations to other agents
        ArrayList<Target> nonBroadcastedBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

        if (nonBroadcastedBatteryStations.size() > 0)
        {
        String batteryStationsString = gson.toJson(nonBroadcastedBatteryStations);
        agentCommunication.broadcastMessage(batteryStationsString);
        System.out.println(String.format("Agent on coordinate (%d,%d) has broadcasted a message", agentState.getX(), agentState.getY()));
        }

        // Get messages from other agents
        Collection<Mail> messages = agentCommunication.getMessages();
        ArrayList<Target> discoveredBatteryStations = AgentGeneralNecessities.getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);

        for (Mail message : messages)
        {
            System.out.println(String.format("Agent on coordinate (%d,%d) has received a message", agentState.getX(), agentState.getY()));
            ArrayList<BatteryStation> newBatteryStations = gson.fromJson(message.getMessage(), new TypeToken<ArrayList<BatteryStation>>(){}.getType());
            
            for (BatteryStation batteryStation : newBatteryStations)
            {
                if (!discoveredBatteryStations.contains(batteryStation))
                {
                    discoveredBatteryStations.add(batteryStation);
                }
            }
        }

        AgentTaskInteraction.updateTaskMemory(agentState, null, null, discoveredBatteryStations, new ArrayList<Target>());
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

        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task and position
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            Coordinate position = task.getDestination().getCoordinate();

            // Move to position
            AgentGeneralNecessities.moveToPosition(agentState, agentAction, position);
        }
        else AgentGeneralNecessities.moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }
}