package util.assignments.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.graph.GraphUtils;

/**
 * A class that implements functions regarding the action of the agent
 */
public class ActionUtils {

    //////////////
    // MOVEMENT //
    //////////////

        final static List<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
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
     * A function to make the agent move randomly
     * 
     * @param agentState The current state of the agent
     * @param agentAction Used to perform an action with the agent
     */
    public static void moveRandomly(AgentState agentState, AgentAction agentAction) {
        // Get the position of the agent
        Perception agentPerception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Get the relative positions
        List<Coordinate> relativePositions = RELATIVE_POSITIONS;

        Collections.shuffle(relativePositions);

        // Loop over all relative positions
        for (Coordinate relativePosition : relativePositions) {
            // Get candidate cell
            int relativePositionX = relativePosition.getX();
            int relativePositionY = relativePosition.getY();
            CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            // Check if the cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) {
                // Calculate the move
                int agentNewX = agentX + relativePositionX;
                int agentNewY = agentY + relativePositionY;

                // Perform a step
                agentAction.step(agentNewX, agentNewY);

                // Inform
                String message = String.format("%s: Moved randomly", agentState.getName());
                System.out.println(message);

                return;
            }
        }

        // Skip if no walkable cell was found
        agentAction.skip();
    }

        /////////////////
        // TO POSITION //
        /////////////////

    /**
     * A function to make the agent move to a position
     * 
     * @param agentAction Perform an action with the agent
     * @param coordinate The coordinate of the position to move to
     */
    public static void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate coordinate) {
        // Check if the position is in the perception of the agent
        if(GeneralUtils.positionInPerception(agentState, coordinate)) {
            Coordinate move = calculateMoveDefault(agentState, coordinate);

            makeMove(agentState, agentAction, move);
        }
        // Check if the position is in the graph
        else if(GeneralUtils.positionInGraph(agentState, coordinate)) {
            Coordinate move = calculateMoveAStar(agentState, coordinate);

            makeMove(agentState, agentAction, move);
        }
        else throw new IllegalArgumentException("Target not in perpcetion nor in graph");
    }

    /**
     * A function to calculate the move (default)
     * 
     * @param agentState The current state of the agent
     * @param target The coordinate of the target
     */
    private static Coordinate calculateMoveDefault(AgentState agentState,  Coordinate target) {
        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int targetX = target.getX();
        int targetY = target.getY();

        // Calculate the difference between the positions
        int dX = targetX - agentX;
        int dY = targetY - agentY;

        // Calculate move
        int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
        int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);

        // Define the move coordinate
        Coordinate moveCoordinate = new Coordinate(relativePositionX, relativePositionY);

        return moveCoordinate;
    }

    /**
     * A function to calculate the move using an A* algorithm
     * 
     * @param agentState The current state of the agent
     * @param target The coordinate of the target
     */
    private static Coordinate calculateMoveAStar(AgentState agentState,  Coordinate target) {
        // Perform A* search
        Coordinate pathCoordinate = GraphUtils.performAStarSearch(agentState, target);

        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int pathCoordinateX = pathCoordinate.getX();
        int pathCoordinateY = pathCoordinate.getY();

        // Calculate the difference between the positions
        int dX = pathCoordinateX - agentX;
        int dY = pathCoordinateY - agentY;

        // Calculate move
        int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
        int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);

        // Define the move coordinate
        Coordinate moveCoordinate = new Coordinate(relativePositionX, relativePositionY);

        return moveCoordinate;
    }

    /**
     * A function to let the agent make a move
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param move The coordinate representing the move
     */
    private static void makeMove(AgentState agentState, AgentAction agentAction, Coordinate move) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int moveX = move.getX();
        int moveY = move.getY();

        // Get corresponding cell
        CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(moveX, moveY);

        // Check if the cell is walkable
        if (cellPerception != null && cellPerception.isWalkable()) {
            // Calculate the move
            int agentNewX = agentX + moveX;
            int agentNewY = agentY + moveY;

            // Perform a step
            agentAction.step(agentNewX, agentNewY);

            // Inform
            String message = String.format("%s: Moved to position (%d,%d)", agentState.getName(), agentNewX, agentNewY);
            System.out.println(message);
        }
        else ActionUtils.moveRandomly(agentState, agentAction);
    }

    ////////////
    // PACKET //
    ////////////

    /**
     * A function to perform the pick up
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param packeCoordinate The coordinate of the packet to pick up
     */
    public static void pickUpPacket(AgentState agentState, AgentAction agentAction, Coordinate packetCoordinate) {
        // Get the position
        int packetX = packetCoordinate.getX();
        int packetY = packetCoordinate.getY();

        // Perform pick up
        agentAction.pickPacket(packetX, packetY);

        // Inform
        String message = String.format("%s: Picked up packet %s", agentState.getName(), packetCoordinate.toString());
        System.out.println(message);
    }

    /**
     * A function to perform the put down
     * 
     * @param agentState The current state of the agent
     * @param agentAction Perform an action with the agent
     * @param destinationCoordinate The coordinate of the destination where to put down the packet
     */
    public static void putDownPacket(AgentState agentState, AgentAction agentAction, Coordinate destinationCoordinate) {
        // Get the position
        int destinationX = destinationCoordinate.getX();
        int destinationY = destinationCoordinate.getY();

        // Perfom put down
        agentAction.putPacket(destinationX, destinationY);

        // Inform
        String message = String.format("%s: Put down packet %s", agentState.getName(), destinationCoordinate.toString());
        System.out.println(message);
    }
}
