package agent.behavior.assignment_1_B;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Mail;
import environment.Perception;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.targets.BatteryStation;
import util.targets.Target;
import util.task.AgentTaskInteraction;

public class MoveRandomBehavior extends Behavior {

    final ArrayList<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
        new Coordinate(1, 1), 
        new Coordinate(-1, -1),
        new Coordinate(1, 0), 
        new Coordinate(-1, 0),
        new Coordinate(0, 1), 
        new Coordinate(0, -1),
        new Coordinate(1, -1), 
        new Coordinate(-1, 1)
    ));

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

        // Move randomly
        moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }

    /////////////
    // METHODS //
    /////////////
 
    /**
     * Move randomly
     *
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     */
    private void moveRandom(AgentState agentState, AgentAction agentAction) {
        // Retrieve position
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();


        List<Coordinate> positions = RELATIVE_POSITIONS;

        // Prioritize going straight first
        Coordinate previousPosition = AgentGraphInteraction.getPreviousPosition(agentState);
        int vecX = agentState.getX() - previousPosition.getX();
        int vecY = agentState.getY() - previousPosition.getY();
        int dx = Integer.signum(vecX);
        int dy = Integer.signum(vecY);

        Coordinate inFront = new Coordinate(dx, dy);
        positions.remove(inFront);

        // Shuffle relative positions and add the coordinate for going straight in the front
        Collections.shuffle(positions);
        positions.add(0, inFront);

        // Loop over all relative positions
        for (Coordinate relativePosition : positions) {
            // Calculate move
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            //Check if cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;

                // Perform a step
                agentAction.step(newPositionX, newPositionY);


                return;
            }
        }

        agentAction.skip();
    }

    
}