package agent.behavior.assignment_1_B;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Destination;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_B.utils.MemoryKeys;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;

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
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        System.out.println("[MoveRandomBehavior] act");

        checkPerception(agentState);
        
        moveRandom(agentState, agentAction);
    }

    /////////////
    // METHODS //
    /////////////

    /**
     * Move randomly
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void moveRandom(AgentState agentState, AgentAction agentAction) {
        Perception agentPerception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        Collections.shuffle(RELATIVE_POSITIONS);

        for (Coordinate relativePosition : RELATIVE_POSITIONS) {
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;
                
                agentAction.step(newPositionX, newPositionY);
                
                return;
            }
        }

        agentAction.skip();
    }

    /**
     * Check the perception of the agent
     *  
     * @param agentState The current state of the agent
     */
    private void checkPerception(AgentState agentState) {
        Perception perception = agentState.getPerception();
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        
        ArrayList<Packet> discoveredPackets = new ArrayList<Packet>();
        ArrayList<Destination> discoveredDestinations = new ArrayList<Destination>();
        Task task = null;
        Gson gson = new Gson();
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) {
            String discoveredPacketsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
            discoveredPackets = gson.fromJson(discoveredPacketsString, new TypeToken<List<Packet>>(){}.getType());
        }
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) {
            String discoveredDestinationsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);
            discoveredDestinations = gson.fromJson(discoveredDestinationsString, new TypeToken<List<Destination>>(){}.getType());
        }
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            task = Task.fromJson(taskString);
        }

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
                    }
                }
            }
        }

        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);
        String discoveredPacketsString = gson.toJson(discoveredPackets);
        String discoveredDestinationsString = gson.toJson(discoveredDestinations);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);
    }
}