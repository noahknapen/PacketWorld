package agent.behavior.assignment_1_B;

import java.awt.Color;
import java.util.ArrayList;
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

public class MoveToDestinationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        System.out.println("[MoveToDestinationBehavior]{act}");
        
        // Check perception
        checkPerception(agentState);

        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);

            // Move to position
            moveToPosition(agentState, agentAction, task);
        }
        else agentAction.skip();;
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
        ArrayList<Packet> discoveredPackets = getDiscoveredPackets(agentState);
        ArrayList<Destination> discoveredDestinations = getDiscoveredDestinations(agentState);
        Task task = getTask(agentState);

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

                        System.out.println("[MoveToDestinationBehavior]{checkPerception} New destination discovered (" + discoveredDestinations.size() + ")");
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

                        System.out.println("[MoveToDestinationBehavior]{checkPerception} New packet discovered (" + discoveredPackets.size() + ")");
                    }
                }
            }
        }

        // Update memory
        updateMemory(agentState, discoveredPackets, discoveredDestinations);        
    }

    /**
     * Move towards a specific position
     * 
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     * @param task Current task
     */
    private void moveToPosition(AgentState agentState, AgentAction agentAction, Task task) {
        // Retrieve position
        Perception agentPerception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int positionX = task.getDestination().getCoordinate().getX();
        int positionY = task.getDestination().getCoordinate().getY();

        // Calculate move
        int dX = positionX - agentX;
        int dY = positionY - agentY;
        int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
        int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);
        CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

        // Check if cell is walkable
        if (cellPerception != null && cellPerception.isWalkable()) {
            int newPositionX = agentX + relativePositionX;
            int newPositionY = agentY + relativePositionY;

            // Perform a step 
            agentAction.step(newPositionX, newPositionY);

            System.out.println("[MoveToDestinationBehavior]{moveToPosition} Agent: (" + agentX + ", " + agentY + ") Position: (" + positionX + ", " + positionY + ")");
        }
        else agentAction.skip();
    }

    /**
     * Retrieve discovered packets from memory
     * Create list if not yet created
     * 
     * @param agentState Current state of agent
     * @return List of discovered packets
     */
    private ArrayList<Packet> getDiscoveredPackets(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered packets exists in memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_PACKETS)) {
            // Retrieve list of discovered packets 
            String discoveredPacketsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
            return gson.fromJson(discoveredPacketsString, new TypeToken<ArrayList<Packet>>(){}.getType());
        }
        else {
            // Create list of discovered packets
            ArrayList<Packet> discoveredPackets = new ArrayList<Packet>();

            // Add list of discovered packets to memory
            String discoveredPacketsString = gson.toJson(discoveredPackets);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);

            return discoveredPackets;
        }
    }

    /**
     * Retrieve discovered destinations from memory
     * Create list if not yet created
     * 
     * @param agentState Current state of agent
     * @return List of discovered destinations
     */ 
    private ArrayList<Destination> getDiscoveredDestinations(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if list of discovered destinations exists in memory
        if(memoryFragments.contains(MemoryKeys.DISCOVERED_DESTINATIONS)) {
            // Retrieve list of discovered destinations 
            String discoveredDestinationsString = agentState.getMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);
            return gson.fromJson(discoveredDestinationsString, new TypeToken<ArrayList<Destination>>(){}.getType());
        }
        else {
            // Create list of discovered destinations
            ArrayList<Destination> discoveredDestinations = new ArrayList<Destination>();

            // Add list of discovered destinations to memory
            String discoveredDestinationsString = gson.toJson(discoveredDestinations);
            agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);

            return discoveredDestinations;
        }
    }

    /**
     * Retrieve task from memory
     * 
     * @param agentState Current state of agent
     * @return Task
     */
    private Task getTask(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            return Task.fromJson(taskString);
        }
        else return null;
    }

    /**
     * Update memory of agent
     * 
     * @param agentState Current state of the agent
     * @param discoveredPackets List of discovered packets
     * @param discoveredDestinations List of discovered destinations
     */
    private void updateMemory(AgentState agentState, ArrayList<Packet> discoveredPackets, ArrayList<Destination> discoveredDestinations) {
        // Remove discovered packets and discovered destinations from memory
        agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_PACKETS);
        agentState.removeMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS);

        // Add updated discovered packets and updated discovered destinations to memory
        Gson gson = new Gson();
        String discoveredPacketsString = gson.toJson(discoveredPackets);
        String discoveredDestinationsString = gson.toJson(discoveredDestinations);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_PACKETS, discoveredPacketsString);
        agentState.addMemoryFragment(MemoryKeys.DISCOVERED_DESTINATIONS, discoveredDestinationsString);
        
        System.out.println("[MoveToDestinationBehavior]{updateMemory} Discovered packets and discovered destinations updated in memory");
    }    
}