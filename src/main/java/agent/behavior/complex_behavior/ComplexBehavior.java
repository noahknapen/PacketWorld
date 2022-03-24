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

public class ComplexBehavior extends Behavior {

    private Graph graph;

    // List of discovered destinations
    // TODO: Save multiple destinations with same color
    ArrayList<Destination> discoveredDestinations = new ArrayList<>();

    // List (queue) of packets to be delivered
    ArrayList<Packet> toBeDeliveredPackets = new ArrayList<Packet>();

    // Current task that is handled
    Task task;

    private HashMap<Coordinate, Color> packetCells = new HashMap<>();
    private HashMap<Color, Coordinate> destinationCells = new HashMap<>();
    private Coordinate moveCoordinate;
    private int counter = 0;

    private String behavior = "explore";

    private Coordinate edgeStartPos;
    private Coordinate prePos;


    private Coordinate unexploredNode = new Coordinate(4, 1);
    private List<Coordinate> path;
    private Coordinate pathDestination;

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
        if (graph == null) {
            graph = new Graph(agentState.getX(), agentState.getY());
            Coordinate currPos = new Coordinate(agentState.getX(), agentState.getY());
            edgeStartPos = currPos;
            prePos = currPos;
        }


        // ---------------- Action Step ----------------
        // If follow wall mode
        //     if behavior == explore
        //          Check going straight cell first (if it is close to wall)
        //          Then check the other free cells (that are close to wall)
        //      else
        //         behaviorchange(explore)
        //

        // If explore mode
        //     neigbours = lookForWalls()
        //     if non visited neigbour next to wall exists && neigbour not in graph paths
        //         path start = curr pos
        //         prepos = curr pos
        //         behaviorchange(followwall)

        //     else
        //         performRandomAction()


        // Handle the graph mapping
        handleGraph(agentState);

        // Handle state
        discoverItem(agentState);

        // Handle action
        handleAction(agentState, agentAction);

        counter++;
        prePos = currPos;

    }

    private void handleGraph(AgentState agentState) {
        int currX = agentState.getX();
        int cuurY = agentState.getY();

        currPos = new Coordinate(currX, cuurY);

        /*if (graph.nodeExists(currPos) && !edgeStartPos.equals(currPos)) {
            graph.addEdge(edgeStartPos, currPos);
        }*/

        if (!edgeStartPos.equals(prePos) && !prePos.equals(currPos)) {
            if (!graph.onTheLine(edgeStartPos, currPos, prePos))
            {
                if (!graph.nodeExists(prePos))
                    graph.addFreeNode(prePos);
                graph.addEdge(edgeStartPos, prePos);
                edgeStartPos = prePos;
                System.out.println("Added new edge");
            }
        }
    }

    private void doExplore(AgentState agentState, AgentAction agentAction) {
        if (moveCoordinate == null) {
            HashMap<String, ArrayList<Coordinate>> neighbours = lookForWalls(agentState.getPerception());
            Collections.shuffle(neighbours.get("free"));

            for (Coordinate freeCell : neighbours.get("free")) {
                if (closeTo(freeCell, neighbours.get("walls")) && !graph.nodeExists(freeCell)) {
                    // step freecell
                }
            }
            System.out.println("Hej");
        }
    }

    private boolean closeTo(Coordinate freeCell, ArrayList<Coordinate> walls) {
        for (Coordinate wall : walls) {
            int distX = Math.abs(wall.getX() - freeCell.getX());
            int distY = Math.abs(wall.getY() - freeCell.getY());

            // If at least one wall is
            if (distX == 1 || distY == 1) {
                return true;
            }
        }
        return false;
    }

    private HashMap<String, ArrayList<Coordinate>> lookForWalls(Perception perception) {
        HashMap<String, ArrayList<Coordinate>> moves = new HashMap<>();
        moves.put("walls", new ArrayList<>());
        moves.put("free", new ArrayList<>());
        for (Coordinate move : possibleMoves) {
            if (perception.getCellPerceptionOnRelPos(move.getX(), move.getY()) == null) {
                moves.get("walls").add(move);
            }
            else if (Objects.requireNonNull(perception.getCellPerceptionOnRelPos(move.getX(), move.getY())).isWalkable()){
                moves.get("free").add(move);
            }
        }
        return moves;
    }

    private List<Coordinate> perceptionSearch(AgentState agentState, Coordinate p) {
        int distX = p.getX() - agentState.getX();
        int distY = p.getY() - agentState.getY();
        int minDist = Math.min(distX, distY);
        int dx = Integer.signum(distX);
        int dy = Integer.signum(distY);

        List<Coordinate> path = new ArrayList<>();
        for (int i = 0; i < minDist; i++) {
            path.add(new Coordinate(dx, dy));
        }

        if (distX > distY) {
            for (int i = 0; i < (distX - minDist); i++) {
                path.add(new Coordinate(dx, 0));
            }
        } else if (distY > distX) {
            for (int i = 0; i < (distY - minDist); i++) {
                path.add(new Coordinate(0, dy));
            }
        }

        return path;
    }

    private void addDestinationToGraph(AgentState agentState, Destination dest) {
        List<Coordinate> possibleCoords = getPossibleNodesAround(dest.getCoordinate(), agentState);
        Coordinate agentCoord = new Coordinate(agentState.getX(), agentState.getY());
        // Coordinate destinationCoord = graph.closestCoordinate(possibleCoords, agentCoord);

        // If closest destination node to agent is the position of the agent itself -> Just add the destination node.
        if (!graph.nodeExists(agentCoord)) {
            graph.addFreeNode(agentCoord);
            graph.addEdge(edgeStartPos, agentCoord);
            edgeStartPos = agentCoord;
        }

        graph.addDestinationNode(dest.getCoordinate(), dest.getColor());

        // TODO: Check if path is free from obstacles
        graph.addEdge(agentCoord, dest.getCoordinate(), "destination", dest.getColor());

    }



    private void addPacketToGraph(AgentState agentState, Packet packet) {
        // TODO: This and addDestinationToGraph are similar -> Maybe its possible to restructure to reuse the same code.
        List<Coordinate> possibleCoords = getPossibleNodesAround(packet.getCoordinate(), agentState);
        Coordinate agentCoord = new Coordinate(agentState.getX(), agentState.getY());
        // Coordinate packetCoord = graph.closestCoordinate(possibleCoords, agentCoord);

        // If closest packet node to agent is the position of the agent itself -> Just add the destination node.
        if (!graph.nodeExists(agentCoord)) {
            graph.addFreeNode(agentCoord);
            graph.addEdge(edgeStartPos, agentCoord);
            edgeStartPos = agentCoord;
        }

        graph.addPacketNode(packet.getCoordinate(), packet.getColor());

        // TODO: Check if path is free from obstacles
        graph.addEdge(agentCoord, packet.getCoordinate(), "packet", packet.getColor());
    }

    private List<Coordinate> getPossibleNodesAround(Coordinate dest, AgentState agentState) {
        ArrayList<Coordinate> moves = new ArrayList<>(List.of(
                new Coordinate(1, 1),
                new Coordinate(-1, -1),
                new Coordinate(1, 0),
                new Coordinate(-1, 0),
                new Coordinate(0, 1),
                new Coordinate(0, -1),
                new Coordinate(1, -1),
                new Coordinate(-1, 1)
        ));

        // Check if positions are walkable
        List<Coordinate> possibleCoords = new ArrayList<>();
        for (Coordinate move : moves) {
            int x = dest.getX() + move.getX();
            int y = dest.getY() + move.getY();

            if (agentState.getPerception().getCellPerceptionOnAbsPos(x, y) != null &&
                    ((Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnAbsPos(x, y)).isWalkable()) ||
                    Objects.requireNonNull(agentState.getPerception().getCellPerceptionOnAbsPos(x, y)).containsAgent()))

                possibleCoords.add(new Coordinate(x, y));
        }
        return possibleCoords;
    }



    // ------------------------------------- Packet and destination handling ----------------------------------

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
     * Handle an action
     *
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     */
    private void handleAction(AgentState agentState, AgentAction agentAction) {
        // Define a task
        defineTask(agentState);

        // Perform the defined action
        //performAction(agentState, agentAction);


        int dx = 0;
        int dy = 0;

        if (counter >= 0) {
            dx = 1;
            dy = 0;
        }

        if (counter >= 2) {
            dx = 1;
            dy = 1;
        }

        if (counter < 6) {
            agentAction.step(agentState.getX() + dx, agentState.getY() + dy);
        }
        else
        {
            performAction(agentState, agentAction);
        }

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

            if (pathDestination != null && pathDestination.equals(position) && !path.isEmpty()) {
                // Pop first coordinate in path and step on it.
                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());
            }
            else if (!graph.nodeExists(currPos)) {
                // TODO: Might not need to recompute closest node every time
                Coordinate closestNodeCoordinate = graph.closestFreeNodeCoordinate(currPos);
                moveToPosition(agentState, agentAction, closestNodeCoordinate);
            }
            else
            {
                // Perform Dijkstras algorithm
                // TODO: Change to Astar???
                pathDestination = position;
                path = graph.doSearch(currPos, position);
                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());
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

