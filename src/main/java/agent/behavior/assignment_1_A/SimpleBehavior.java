package agent.behavior.assignment_1_A;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import agent.behavior.assignment_1_A.utils.Destination;
import agent.behavior.assignment_1_A.utils.Packet;
import agent.behavior.assignment_1_A.utils.PacketComparator;
import agent.behavior.assignment_1_A.utils.Task;
import agent.behavior.assignment_1_A.utils.TaskState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * A class representing a simple behaviour of packet delivery
 */
public class SimpleBehavior extends Behavior {

    // List of discovered destinations
    // TODO: Save multiple destinations with same color
    ArrayList<Destination> discoveredDestinations = new ArrayList<>();

    // List (queue) of packets to be delivered
    ArrayList<Packet> toBeDeliveredPackets = new ArrayList<Packet>();

    // Current task that is handled
    Task task;

    ////////////////////
    // DEFAULT METHODS//
    ////////////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Handle state
        discoverItem(agentState);

        // Handle action
        handleAction(agentState, agentAction);
    }

    /////////////////
    // MAIN METHODS//
    /////////////////
    
    /**
     * Check the perception of the agent to discover:
     * - New destinations
     * - New packets
     * 
     * @param agentState The current state of the agent
     */
    private void discoverItem(AgentState agentState) {
        Perception perception = agentState.getPerception();

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
                        System.out.println("[discoverItems] New destination discovered (" + discoveredDestinations.size() + ")");
                    }
                }
                // Check if current cell contains a packet
                else if(cell.containsPacket()) {
                    Color packetColor = cell.getRepOfType(PacketRep.class).getColor();
                    
                    Packet packet= new Packet(cellCoordinate, packetColor);

                    // Check if packet was not discoverd yet
                    if(toBeDeliveredPackets.contains(packet)) continue;
                    // Check if packet is not currently handled (hence should not be added to list again)
                    else if(task != null && task.getPacket().equals(packet)) continue;
                    else {
                        toBeDeliveredPackets.add(packet);
                        System.out.println("[discoverItems] New packet discovered (" + toBeDeliveredPackets.size() + ")");
                    }
                }
            }
        }
    }

    /**
     * Handle an action
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handleAction(AgentState agentState, AgentAction agentAction) {
        // Define a task
        defineTask(agentState);

        // Perform the defined action
        performAction(agentState, agentAction);
    }

    /////////////////
    // SIDE METHODS//
    /////////////////

    /**
     * Define a task based on the (past) perception
     * 
     * @param agentState The current state of the agent
     */
    private void defineTask(AgentState agentState) {
        // Check if a task is still be handled
        if(task != null) return;

        // Create a default random task
        task = new Task(null, null, TaskState.RANDOM);

        // Check if there is something to deliver
        if(toBeDeliveredPackets.isEmpty()) return;

        // Sort the packets to be delivered
        PacketComparator packComparator = new PacketComparator(agentState, discoveredDestinations);
        Collections.sort(toBeDeliveredPackets, packComparator);

        // Loop through the sorted list of packets to be delivered
        for(int i = 0; i < toBeDeliveredPackets.size(); i++) {
            // Define a candidate packet
            Packet candidatepacket= toBeDeliveredPackets.get(i);
            Color candidatePackColor = candidatepacket.getColor();
            
            // Loop through the list of discovered destinations
            for(int j = 0; j < discoveredDestinations.size(); j++) {
                Color destinationColor = discoveredDestinations.get(j).getColor();
                
                // Check if a corresponding (color) destination was already discovered
                if(candidatePackColor.equals(destinationColor)) {
                    Destination destination = discoveredDestinations.get(j);

                    // Remvoe the packet from the list
                    candidatepacket= toBeDeliveredPackets.remove(i);

                    // Redefine the task
                    task.setPacket(candidatepacket);
                    task.setDestination(destination);
                    task.setTaskState(TaskState.TO_PACKET);

                    return;
                }

            }

        }
    }

    /**
     * Perform an action based on the defined task
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void performAction(AgentState agentState, AgentAction agentAction) {
        // Check if no task was defined
        if(task == null) return;

        // Check the task state
        switch(task.getTaskState()) {
            case RANDOM:
                // Move randomly
                moveRandom(agentState, agentAction);

                // Reset the task
                task = null;

                break;
            case TO_PACKET:
                Coordinate packCoordinate = task.getPacket().getCoordinate();

                // Check if packet was already handled by another agent
                if(packetAlreadyHandled(agentState)) {
                    // Skip this turn
                    agentAction.skip();

                    // Reset the task
                    task = null;
                }
                // Check if position reached
                else if(positionReached(agentState, packCoordinate)) {
                    // Pick up packet
                    pickPacket(agentAction);

                    // Redefine task state
                    task.setTaskState(TaskState.TO_DESTINATION);
                }
                // Position not reached yet, so make a step towards position
                else moveToPosition(agentState, agentAction, packCoordinate);

                break;
            case TO_DESTINATION:
                Coordinate destinationCoordinate = task.getDestination().getCoordinate();

                // Check if position reached
                if(positionReached(agentState, destinationCoordinate)) {
                    // Put down packet
                    putPacket(agentAction);

                    // Reset the task
                    task = null;
                }
                // Position not reached yet, so make a step towards position
                else moveToPosition(agentState, agentAction, destinationCoordinate);

                break;
            default:
                agentAction.skip();
                break;
        }
    }

    /**
     * Pick up a packet
     * 
     * @param agentAction Perform an action with the agent
     */
    private void pickPacket(AgentAction agentAction) {
        System.out.println("[pickPacket] Packet picked up (" + task.getPacket().getColor() + ")");

        agentAction.pickPacket(task.getPacket().getCoordinate().getX(), task.getPacket().getCoordinate().getY());
    }

    /* Pick down a packet
    * 
    * @param agentAction Perform an action with the agent
    */
    private void putPacket(AgentAction agentAction) {
        System.out.println("[pickPacket] Packet put down (" + task.getPacket().getColor() + ")");

        agentAction.putPacket(task.getDestination().getCoordinate().getX(), task.getDestination().getCoordinate().getY());
    }

    /**
     * Move towards a specific position
     * TODO: Make more efficient
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param position The position to move towards
     */
    private void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int positionX = position.getX();
        int positionY = position.getY();

        System.out.println("[moveToPosition] Agent: (" + agentX + ", " + agentY + ") Position: (" + positionX + ", " + positionY + ")");

        // Check if position is in current perception
        if(positionInPerception(agentState, position)) {
            int dx = positionX - agentX;
            int dy = positionY - agentY;
            int dxStep = dx > 0 ? 1 : (dx < 0 ? -1 : 0);
            int dyStep = dy > 0 ? 1 : (dy < 0 ? -1 : 0);

            if (agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep) != null && Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep)).isWalkable()) {
                int newPositionX = agentX + dxStep;
                int newPositionY = agentY + dyStep;
                
                System.out.println("\t\t Agent: (" + newPositionX + ", " + newPositionY + ")");

                // Make a step towards position
                agentAction.step(newPositionX, newPositionY);
            }
            else {
                System.out.println("\t\t Random move");

                // Make a random step
                moveRandom(agentState, agentAction);  
            }
        }
        else {
            System.out.println("\t\t Random move");

            // Make a random step
            moveRandom(agentState, agentAction);
        }        
    }

    /**
     * Move randomly
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void moveRandom(AgentState agentState, AgentAction agentAction) {
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        ArrayList<Coordinate> positions = new ArrayList<>(List.of(
                new Coordinate(1, 1), 
                new Coordinate(-1, -1),
                new Coordinate(1, 0), 
                new Coordinate(-1, 0),
                new Coordinate(0, 1), 
                new Coordinate(0, -1),
                new Coordinate(1, -1), 
                new Coordinate(-1, 1)
        ));

        Collections.shuffle(positions);

        for (Coordinate position : positions) {
            int dxStep = position.getX();
            int dyStep = position.getY();

            if (agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep) != null && Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep)).isWalkable()) {
                int newPositionX = agentX + dxStep;
                int newPositionY = agentY + dyStep;
                
                agentAction.step(newPositionX, newPositionY);
                
                return;
            }
        }

        agentAction.skip();
    }

    //////////////////
    // EXTRA METHODS//
    //////////////////

    /**
     * Check if the packet was already handled by another agent
     * 
     * @param agentState The current state of the agent
     * @return True is packet is not at initial place, otherwise false
     */
    private boolean packetAlreadyHandled(AgentState agentState) {
        Perception perception = agentState.getPerception();

        Packet packet= task.getPacket();
        int packetX = packet.getCoordinate().getX();
        int packetY = packet.getCoordinate().getY();
        
        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                int cellX = cell.getX();
                int cellY = cell.getY();
                
                // Check if coordinates correspond
                if(cellX == packetX && cellY == packetY) {
                    return !cell.containsPacket();
                }
            }
        }

        return false;
    }

    /**
     * Check if position is reached
     * 
     * @param agentState The current state of the agent
     * @param position The position to reach
     * @return True if agent is next to position
     */
    private boolean positionReached(AgentState agentState, Coordinate position) {
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int positionX = position.getX();
        int positionY = position.getY();

        int dx = Math.abs(agentX - positionX);
        int dy = Math.abs(agentY - positionY);
        return (dx <= 1) && (dy <= 1);
    }

    /**
     * Check if position is in the current perception
     * 
     * @param agentState The current state of the agent
     * @param position The position to check
     * @return True if position is in current perception
     */
    private boolean positionInPerception(AgentState agentState, Coordinate position) {
        Perception perception = agentState.getPerception();
        int positionX = position.getX();
        int positionY = position.getY();

        // Loop over whole perception
        for (int x = 0; x < perception.getWidth(); x++) {
            for (int y = 0; y < perception.getHeight(); y++) {
                CellPerception cell = perception.getCellAt(x,y);

                if(cell == null) continue;

                int cellX = cell.getX();
                int cellY = cell.getY();
                
                // Check if coordinates correspond
                if(cellX == positionX && cellY == positionY) return true;
            }
        }

        return false;
    }
}
