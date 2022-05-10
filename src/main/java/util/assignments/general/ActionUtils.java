package util.assignments.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.comparators.CoordinateComparator;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

/**
 * A class that implements functions regarding the action of the agent
 */
public class ActionUtils {

    /////////////
    // GENERAL //
    /////////////

    /**
     * Skip a turn
     *
     * @param agentAction The action interface of the agent
     */
    public static void skipTurn(AgentAction agentAction) {
        agentAction.skip();
    }

    //////////////
    // MOVEMENT //
    //////////////

    private final static List<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
            new Coordinate(1, 1), 
            new Coordinate(-1, -1),
            new Coordinate(1, 0), 
            new Coordinate(-1, 0),
            new Coordinate(0, 1), 
            new Coordinate(0, -1),
            new Coordinate(1, -1), 
            new Coordinate(-1, 1)
        ));

        //////////////
        // RANDOMLY //
        //////////////

    /**
     * Move the agent randomly
     * The agent will try to move in the same direction as long as possible.
     * 
     * @param agentState The current state of the agent
     * @param agentAction The action inteface of the agent
     */
    public static void moveRandomly(AgentState agentState, AgentAction agentAction) {
        // Get the random move coordinate
        // It is the current direction the agent is walking in
        Coordinate randomMoveCoordinate = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.RANDOM_DIRECTION, Coordinate.class);

        // Check if the random move coordinate is not null and the move is possible
        if(randomMoveCoordinate != null && ActionUtils.isMovePossible(agentState, randomMoveCoordinate)) {
            ActionUtils.makeMove(agentState, agentAction, randomMoveCoordinate);
        }
        else {
            // Define new random move
            ActionUtils.defineNewRandomMove(agentState, randomMoveCoordinate);

            // Recall
            ActionUtils.moveRandomly(agentState, agentAction);
        }
    }

        /////////////////
        // TO POSITION //
        /////////////////

    /**
     * Move the agent towards a position
     * If the position is in the graph, follow the graph, otherwise, move random to position.
     * 
     * @param agentState The current state of the agent
     * @param agentAction The action interface of the agent
     * @param coordinate The coordinate of the position to move to
     */
    public static void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate coordinate) {
        // Create move coordinate
        Optional<Coordinate> moveCoordinate = Optional.empty();

        // Check if the coordinate is in the graph
        if(GeneralUtils.positionInGraph(agentState, coordinate)) {
            // Calculate the move coordinate with A*
            moveCoordinate = ActionUtils.calculateMoveAStar(agentState, coordinate);
        }

        // Check if the move is empty or the move is not possible
        if(moveCoordinate.isEmpty() || !ActionUtils.isMovePossible(agentState, moveCoordinate.get())) {
            // Calculate a random move to position
            moveCoordinate = ActionUtils.calculateMoveRandomToPosition(agentState, coordinate);
        }

        // Check if the move is empty or the move is not possible
        if(moveCoordinate.isEmpty() || !ActionUtils.isMovePossible(agentState, moveCoordinate.get())) {
            // Move randomly
            ActionUtils.moveRandomly(agentState, agentAction);
        }
        
        ActionUtils.makeMove(agentState, agentAction, moveCoordinate.get());
    }
    
    /**
     * Calculate the move using an A* algorithm
     * 
     * @param agentState The current state of the agent
     * @param targetCoordinate The coordinate of the target
     * @return The move coordinate if it exists, otherwise empty
     */
    private static Optional<Coordinate> calculateMoveAStar(AgentState agentState,  Coordinate targetCoordinate) {
        // Perform A* search
        Optional<Coordinate> pathCoordinate = GraphUtils.performAStarSearch(agentState, targetCoordinate);

        // Check if the path coordinate is empty and return it if so
        if(pathCoordinate.isEmpty())
            return pathCoordinate;

        // Get the position of the agent and the path coordinate
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int pathCoordinateX = pathCoordinate.get().getX();
        int pathCoordinateY = pathCoordinate.get().getY();

        // Calculate the difference between the positions
        int dX = pathCoordinateX - agentX;
        int dY = pathCoordinateY - agentY;

        // Calculate the move
        int relativePositionX = Integer.compare(dX, 0);
        int relativePositionY = Integer.compare(dY, 0);

        // Create and return the move coordinate
        return Optional.of(new Coordinate(relativePositionX, relativePositionY));
    }

    /**
     * Calculate the move randomly but towards position
     * When the targeted position isn't in the graph, the agents needs to walk randomly to that position.
     * It does so by calculating the lowest distance from its possibilities
     *
     * @param agentState The current state of the agent
     * @param targetCoordinate The coordinate of the target
     * @return The move coordinate if it exists, otherwise empty
     */
    private static Optional<Coordinate> calculateMoveRandomToPosition(AgentState agentState, Coordinate targetCoordinate) {
        // Get the list of possible moves
        ArrayList<Coordinate> randomMoves = new ArrayList<>(ActionUtils.RELATIVE_POSITIONS);

        // Get the random coordinate
        CoordinateComparator coordinateComparator = new CoordinateComparator(agentState, targetCoordinate);
        Optional<Coordinate> randomCoordinate = randomMoves.stream().filter(c -> ActionUtils.isMovePossible(agentState, c)).sorted(coordinateComparator).findFirst();

        return randomCoordinate;
    }

    /**
     * Make a move
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param moveCoordinate The coordinate representing the move
     */
    private static void makeMove(AgentState agentState, AgentAction agentAction, Coordinate moveCoordinate) {
        // Get the position of the agent and the move
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int moveX = moveCoordinate.getX();
        int moveY = moveCoordinate.getY();

        // Calculate the resulting position
        int resultX = agentX + moveX;
        int resultY = agentY + moveY;

        // Perform a step
        agentAction.step(resultX, resultY);

        // Inform
        if (GeneralUtils.PRINT)
            System.out.printf("%s: Moved to position (%d,%d)\n", agentState.getName(), resultX, resultY);
    }

    ////////////
    // PACKET //
    ////////////

    /**
     * Pick up a packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction The action interface of the agent
     * @param packetCoordinate The coordinate of the packet to pick up
     */
    public static void pickUpPacket(AgentState agentState, AgentAction agentAction, Coordinate packetCoordinate) {
        // Get the position of the packet
        int packetX = packetCoordinate.getX();
        int packetY = packetCoordinate.getY();

        // Pick up the packet
        agentAction.pickPacket(packetX, packetY);

        // Inform
        if (GeneralUtils.PRINT)
            System.out.printf("%s: Picked up packet %s\n", agentState.getName(), packetCoordinate);
    }

    /**
     * Put down a packet
     * 
     * @param agentState The current state of the agent
     * @param agentAction The action interface of the agent
     * @param destinationCoordinate The coordinate of the destination where to put down the packet
     */
    public static void putDownPacket(AgentState agentState, AgentAction agentAction, Coordinate destinationCoordinate) {
        // Get the position of the destination
        int destinationX = destinationCoordinate.getX();
        int destinationY = destinationCoordinate.getY();

        // Put down the packet
        agentAction.putPacket(destinationX, destinationY);

        // Inform
        if (GeneralUtils.PRINT)
            System.out.printf("%s: Put down packet %s\n", agentState.getName(), destinationCoordinate);
    }

    ///////////
    // UTILS //
    ///////////

    private static boolean isMovePossible(AgentState agentState, Coordinate moveCoordinate) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the position of the move
        int moveX = moveCoordinate.getX();
        int moveY = moveCoordinate.getY();

        // Get the perception of the cell
        CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(moveX, moveY);

        // Check and returns if the cell is not null and is walkable
        return (cellPerception != null && cellPerception.isWalkable());
    }

    /**
      * Define a new random move
      * It defines a new direction in which the agent should move when it moves randomly
      *
      * @param agentState The current state of the agent
      * @param randomMoveCoordinate The current random move coordinate
      */
    private static void defineNewRandomMove(AgentState agentState, Coordinate randomMoveCoordinate) {
        // Get the list of possible moves
        ArrayList<Coordinate> randomMoves = new ArrayList<>(ActionUtils.RELATIVE_POSITIONS);

        // Create a result move
        Coordinate resultMove = null;

        // Loop over possible random moves
        while(resultMove == null) {
            // Shuffle the list of possible moves
            Collections.shuffle(randomMoves);

            // Create a candidate move
            Coordinate candidateMove = randomMoves.get(0);

            // Check if candidate move equals the current move and loop again if so
            if(candidateMove.equals(randomMoveCoordinate)) {
                continue;
            }

            // Check if the curren move is not null or the candidate move is not possible and loop again if so
            if(randomMoveCoordinate != null && !ActionUtils.isMovePossible(agentState, candidateMove)) {
                continue;
            }

            // Assign the candidate move to the result move
            resultMove = candidateMove;

            break;
        }

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.RANDOM_DIRECTION, resultMove));
    }
}
