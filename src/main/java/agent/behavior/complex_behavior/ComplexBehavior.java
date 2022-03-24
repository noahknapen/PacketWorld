package agent.behavior.complex_behavior;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.tasks.*;
import util.mapping.Graph;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * A class representing a more complex behaviour of packet delivery
 */
public class ComplexBehavior extends Behavior {

    private Graph graph;

    // List of discovered destinations
    // TODO: Save multiple destinations with same color
    ArrayList<Destination> discoveredDestinations = new ArrayList<>();

    // List (queue) of packets to be delivered
    ArrayList<Packet> toBeDeliveredPackets = new ArrayList<Packet>();

    // Current task that is handled
    Task task;

    // For debugPath method.
    private int counter = 0;

    private Coordinate edgeStartPos;
    private Coordinate prePos;

    private List<Coordinate> path = new LinkedList<>();
    private Coordinate shouldBeHerePos;

    List<Coordinate> possibleMoves = new ArrayList<>(List.of(
            new Coordinate(1, 1), new Coordinate(-1, -1),
            new Coordinate(1, 0), new Coordinate(-1, 0),
            new Coordinate(0, 1), new Coordinate(0, -1),
            new Coordinate(1, -1), new Coordinate(-1, 1)
    ));
    private Coordinate currPos;

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // No communication
    }

    
    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        // Initialize the graph
        if (graph == null) {
            graph = new Graph(agentState.getX(), agentState.getY());
            currPos = new Coordinate(agentState.getX(), agentState.getY());
            edgeStartPos = currPos;
            prePos = currPos;
        }

        // Handle the graph mapping
        handleGraph(agentState);

        // Handle state
        discoverItem(agentState);

        // Handle action
        handleAction(agentState, agentAction);

        // Update agents previous position
        prePos = currPos;

    }

    /**
     * Dynamically builds the graph
     * Agent saves potential starts of edges in edgeStartPos.
     *
     * If agent goes in a straight line -> agents previous position lies on the line between
     * edgeStartPos and agents current position.
     *
     * If agent turns -> agents previous position DOES NOT lie on the line between
     * edgeStartPos and agents current position
     * -> create new edge between edgeStartPos and agents previous position.
     *
     * @param agentState The agent state
     */
    private void handleGraph(AgentState agentState) {
        int currX = agentState.getX();
        int cuurY = agentState.getY();

        currPos = new Coordinate(currX, cuurY);

        if (!edgeStartPos.equals(prePos) && !prePos.equals(currPos)) {
            if (!graph.onTheLine(edgeStartPos, currPos, prePos))
            {
                if (!graph.nodeExists(prePos))
                    graph.addFreeNode(prePos);
                graph.addEdge(edgeStartPos, prePos);
                edgeStartPos = prePos;
            }
        }
    }

    /**
     * Adds new destination to graph
     * Draws an edge from:
     *     From edgeStartPos -> agentPos
     *     agentPos -> destinationPos
     * @param agentState The agent state
     * @param dest The new destination to be added to the graph
     */
    private void addDestinationToGraph(AgentState agentState, Destination dest) {
        Coordinate agentCoord = new Coordinate(agentState.getX(), agentState.getY());

        // If agent position is not in the graph -> Add the position and an edge from edgeStartPos.
        if (!graph.nodeExists(agentCoord)) {
            graph.addFreeNode(agentCoord);
            graph.addEdge(edgeStartPos, agentCoord);
            edgeStartPos = agentCoord;
        }

        graph.addDestinationNode(dest.getCoordinate(), dest.getColor());

        // TODO: Check if path is free from obstacles (It should be but not sure)
        graph.addEdge(agentCoord, dest.getCoordinate(), "destination", dest.getColor());

    }

    /**
     * Adds new packet to graph
     * Draws an edge from:
     *     From edgeStartPos -> agentPos
     *     agentPos -> packetPos
     * @param agentState The agent state
     * @param packet The new packet to be added to the graph
     */
    private void addPacketToGraph(AgentState agentState, Packet packet) {
        // TODO: This and addDestinationToGraph are similar -> Maybe its possible to restructure to reuse the same code.
        Coordinate agentCoord = new Coordinate(agentState.getX(), agentState.getY());

        // If agent position is not in the graph -> Add the position and an edge from edgeStartPos.
        if (!graph.nodeExists(agentCoord)) {
            graph.addFreeNode(agentCoord);
            graph.addEdge(edgeStartPos, agentCoord);
            edgeStartPos = agentCoord;
        }

        graph.addPacketNode(packet.getCoordinate(), packet.getColor());

        // TODO: Check if path is free from obstacles (It should be but not sure)
        graph.addEdge(agentCoord, packet.getCoordinate(), "packet", packet.getColor());
    }


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

                    // Update graph if unknown destination in cell
                    if (!graph.nodeExists(cell.getX(), cell.getY())) {

                        // If this destination is not already in the graph -> add it.
                        addDestinationToGraph(agentState, destination);

                    }
                }
                // Check if current cell contains a packet
                else if(cell.containsPacket()) {
                    Color packetColor = cell.getRepOfType(PacketRep.class).getColor();

                    Packet packet= new Packet(cellCoordinate, packetColor);

                    // Check if packet was not discoverd yet
                    if(toBeDeliveredPackets.contains(packet)) continue;
                        // Check if packet is not currently handled (hence should not be added to list again)
                    else if (task != null && task.getPacket() != null && task.getPacket().equals(packet)) continue;
                    else {
                        toBeDeliveredPackets.add(packet);
                        System.out.println("[discoverItems] New packet discovered (" + toBeDeliveredPackets.size() + ")");
                    }

                    // Add node of agent position that says that agent can see packet from position.
                    if (!graph.nodeExists(cell.getX(), cell.getY())) {
                        addPacketToGraph(agentState, packet);
                    }
                }
            }
        }
    }

    /**
     * This method can be used to define a path for the agent in the beginning.
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void debuggingPath(AgentState agentState, AgentAction agentAction) {
        int dx = 0;
        int dy = 0;

        if (counter >= 0) {
            dx = 0;
            dy = 1;
        }

        if (counter >= 1) {
            dx = 1;
            dy = 0;
        }

        if (counter >= 4) {
            dx = 1;
            dy = 1;
        }

        if (counter >= 6) {
            dx = 1;
            dy = 0;
        }

        if (counter < 7) {
            agentAction.step(agentState.getX() + dx, agentState.getY() + dy);
        }
        else
        {
            performAction(agentState, agentAction);
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

        // In case you want to make agent go predefined path. (Don't forget to comment out performAction above if used).
        // debuggingPath(agentState, agentAction);


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

                    // Remove packet node from graph
                    graph.removePacketNode(task.getPacket().getCoordinate());

                    // Reset the task
                    task = null;
                }
                // Check if position reached
                else if(positionReached(agentState, packCoordinate)) {
                    // Pick up packet
                    pickPacket(agentAction);

                    // Remove packet node from graph
                    graph.removePacketNode(task.getPacket().getCoordinate());

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
        int positionX = position.getX(); // TODO: Fix bug - position is null like 1% of the time
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

            // Reset path
            path.clear();
        }
        else
        {

            // If path exists -> Just follow the path.
            if (!path.isEmpty())
            {
                // If previous movement failed for some reason -> Try again.
                if (!currPos.equals(shouldBeHerePos)) {
                    moveToPosition(agentState, agentAction, shouldBeHerePos);
                    return;
                }

                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                shouldBeHerePos = nextCoordinate;
                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());
            }

            // If agent position outside the graph -> Move to the closest node first.
            else if (!graph.nodeExists(currPos))
            {
                Coordinate closestNodeCoordinate = graph.closestFreeNodeCoordinate(agentState.getPerception(), currPos);
                moveToPosition(agentState, agentAction, closestNodeCoordinate);
            }

            // Search for path from current position to the desired position.
            else
            {
                // Perform Dijkstra's algorithm
                path = graph.doSearch(currPos, position);

                if (!path.isEmpty())
                {
                    Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                    shouldBeHerePos = nextCoordinate;
                    agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());
                }
                else
                {
                    moveRandom(agentState, agentAction);
                }
            }
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

        // Prioritize going straight
        int vecX = agentState.getX() - prePos.getX();
        int vecY = agentState.getY() - prePos.getY();
        int dx = Integer.signum(vecX);
        int dy = Integer.signum(vecY);

        Coordinate inFront = new Coordinate(dx, dy);
        positions.remove(inFront);
        Collections.shuffle(positions);
        positions.add(0, inFront);

        for (Coordinate position : positions) {
            int dxStep = position.getX();
            int dyStep = position.getY();

            if (agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep) != null && Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnRelPos(dxStep, dyStep)).isWalkable()) {
                int newPositionX = agentX + dxStep;
                int newPositionY = agentY + dyStep;
                if (agentState.getName().equals("Ana")) {
                    System.out.println("Random move");
                }
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

