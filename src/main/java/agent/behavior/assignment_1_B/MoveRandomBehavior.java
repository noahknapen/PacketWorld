package agent.behavior.assignment_1_B;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

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
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.graph.Graph;
import util.targets.BatteryStation;
import util.targets.Destination;
import util.targets.Packet;
import util.targets.Target;
import util.task.AgentTaskInteraction;
import util.task.Task;

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
        ArrayList<Target> nonBroadcastedBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);

        if (nonBroadcastedBatteryStations.size() > 0)
        {
        String batteryStationsString = gson.toJson(nonBroadcastedBatteryStations);
        agentCommunication.broadcastMessage(batteryStationsString);
        System.out.println(String.format("Agent on coordinate (%d,%d) has broadcasted a message", agentState.getX(), agentState.getY()));
        }

        // Get messages from other agents
        Collection<Mail> messages = agentCommunication.getMessages();
        ArrayList<Target> discoveredBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);

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
        checkPerception(agentState);

        // Move randomly
        moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Check perception of agent
     *  
     * @param agentState Current state of agent
     */
    private void checkPerception(AgentState agentState) {
        // Retrieve discovered packets, discovered destinations and task
        Perception perception = agentState.getPerception();
        ArrayList<Target> discoveredPackets = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_PACKETS);
        ArrayList<Target> discoveredDestinations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_DESTINATIONS);
        ArrayList<Target> discoveredBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.DISCOVERED_BATTERY_STATIONS);
        ArrayList<Target> nonBroadcastedBatteryStations = getDiscoveredTargetsOfSpecifiedType(agentState, MemoryKeys.NON_BROADCASTED_BATTERY_STATIONS);
        Task task = AgentTaskInteraction.getTask(agentState);
        Graph graph = AgentGraphInteraction.getGraph(agentState);

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                Coordinate cellCoordinate = new Coordinate(cell.getX(), cell.getY());

                // Check if current cell contains a destination
                if(cell.containsAnyDestination()) {
                    Color destinationColor = cell.getRepOfType(DestinationRep.class).getColor();

                    Destination destination = new Destination(cellCoordinate, destinationColor);

                    // Check if destination was not discoverd yet
                    if(discoveredDestinations.contains(destination)) continue;
                    else {
                        discoveredDestinations.add(destination);

                        System.out.println("[MoveRandomBehavior]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");
                    }

                    // Update graph if unknown destination in cell
                    if(!graph.nodeExists(cell.getX(), cell.getY())) {
                        // If this destination is not already in the graph -> add it
                        AgentGraphInteraction.addTargetToGraph(agentState, destination);
                    }
                }
                // Check if current cell contains a packet
                else if(cell.containsPacket()) {
                    Color packetColor = cell.getRepOfType(PacketRep.class).getColor();
                    
                    Packet packet= new Packet(cellCoordinate, packetColor);

                    // Check if packet was not discoverd yet
                    if(discoveredPackets.contains(packet)) continue;
                    // Check if packet is not currently handled (hence should not be added to list again)
                    else if(task != null && task.getPacket().equals(packet)) continue;
                    else {
                        discoveredPackets.add(packet);

                        System.out.println("[MoveRandomBehavior]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");
                    }

                    // Add node of agent position that says that agent can see packet from position.
                    if (!graph.nodeExists(cell.getX(), cell.getY())) {
                        AgentGraphInteraction.addTargetToGraph(agentState, packet);
                    }
                } else if (cell.containsEnergyStation())
                {
                    BatteryStation batteryStation = new BatteryStation(cellCoordinate);

                    if (discoveredBatteryStations.contains(batteryStation))
                        continue;
                    else
                    {
                        discoveredBatteryStations.add(batteryStation);
                        nonBroadcastedBatteryStations.add(batteryStation);
                        System.out.println(String.format("[MoveRandomBehavior]{checkPerception} Agent on location (%d,%d) has discovered a new battery station (" + discoveredBatteryStations.size() + ")", agentState.getX(), agentState.getY()));
                    }

                    if (!graph.nodeExists(cell.getX(), cell.getY()))
                    {
                        AgentGraphInteraction.addTargetToGraph(agentState, batteryStation);
                    }
                }
            }
        }

        // Update memory
        AgentTaskInteraction.updateTaskMemory(agentState, discoveredPackets, discoveredDestinations, discoveredBatteryStations, nonBroadcastedBatteryStations);        
    } 
 
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

    /** Retrieve specified type of targets from memory
     * Create a list if this type of targets has not yet been created
     * 
     * @param agentState The current state of the agent
     * @param memoryKey The string specifying the key specifying the memoryfragment. This string can be fetched from the {@code MemoryKeys} class.
     * @return List of discovered targets of this type
     */
    private ArrayList<Target> getDiscoveredTargetsOfSpecifiedType(AgentState agentState, String memoryKey)
    {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered packets exists in memory
        if(memoryFragments.contains(memoryKey)) {
            // Retrieve list of discovered packets 
            String discoveredTargetsString = agentState.getMemoryFragment(memoryKey);
            return gson.fromJson(discoveredTargetsString, new TypeToken<ArrayList<Target>>(){}.getType());
        }
        else {
            // Create list of discovered packets
            ArrayList<Target> discoveredTargets = new ArrayList<Target>();

            // Add list of discovered packets to memory
            String discoveredTargetsString = gson.toJson(discoveredTargets);
            agentState.addMemoryFragment(memoryKey, discoveredTargetsString);

            return discoveredTargets;
        }

    }  
}