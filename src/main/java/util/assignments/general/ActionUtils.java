package util.assignments.general;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.assignments.exceptions.NoMoveFoundException;
import util.assignments.graph.GraphUtils;

/**
 * A class that implements functions regarding the action of the agent
 */
public class ActionUtils {

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
     * A function to make the agent move randomly. The agent will however prioritize position that aren't located
     * in the graph.
     * 
     * @param agentState The current state of the agent
     * @param agentAction Used to perform an action with the agent
     */
    public static void moveRandomly(AgentState agentState, AgentAction agentAction) {
        // Get the position of the agent
        Perception agentPerception = agentState.getPerception();

        // Retrieves all the neighbours of the agent
        ArrayList<CellPerception> neighbours = agentPerception.getNeighbours();

        // Shuffle for randomness
        Collections.shuffle(neighbours);

        // Iterate through all the neighbours
        for (CellPerception neighbour : neighbours) {
            // If the neighbour isn't walkable or is null -> skip
            if (neighbour == null || !neighbour.isWalkable()) continue;

            // Perform step
            agentAction.step(neighbour.getX(), neighbour.getY());

            // Inform dev
            System.out.printf("%s: Moved randomly to %s %s\n", agentState.getName(),neighbour.getX(), neighbour.getY());

            return;

        }

        // Skip if no walkable cell was found
        skipTurn(agentAction);
    }

        /////////////////
        // TO POSITION //
        /////////////////

    /**
     * A function to make the agent move towards a position. If the position is in the perception, calculate a default move.
     * If the position is in the graph, follow the graph. If the position is neither in the graph nor perception,
     * move random to the position.
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
        else {
            // If not in the graph, move closer to the position
            moveRandomToPosition(agentState, agentAction, coordinate);
        }
    }

    /**
     * When the targeted position isn't in the graph, the agents needs to walk randomly to that position. It does so by
     * calculating the lowest distance from its possibilities
     *
     * @param agentState The current state of the agent
     * @param agentAction The action interface for the agent
     * @param targetCoordinate The position we want to go to
     */
    private static void moveRandomToPosition(AgentState agentState, AgentAction agentAction, Coordinate targetCoordinate) {
        // Make the agent position
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        // Two variables for determining which is the best coordinate
        double minDistance = Double.MAX_VALUE;
        Coordinate bestCoordinate = agentPosition;

        // Iterate through all the relative positions
        for (Coordinate relativePosition : RELATIVE_POSITIONS) {
            // Calculate move
            CellPerception cellPerception = agentState.getPerception().getCellPerceptionOnRelPos(relativePosition.getX(), relativePosition.getY());

            //Check if cell is walkable
            if (cellPerception == null || !cellPerception.isWalkable()) continue;

            // Create a variable of the position to try
            int newPositionX = agentState.getX() + relativePosition.getX();
            int newPositionY = agentState.getY() + relativePosition.getY();
            Coordinate newPosition = new Coordinate(newPositionX, newPositionY);

            // Check if cell is a better option
            if (ActionUtils.calculateDistance(newPosition, targetCoordinate) > minDistance) continue;

            // It is a better option so change the min distance and the relative position
            minDistance = ActionUtils.calculateDistance(newPosition, targetCoordinate);
            bestCoordinate = relativePosition;
        }

        // If the bestCoordinate is the agentPosition, move randomly
        if (agentPosition.equals(bestCoordinate)) moveRandomly(agentState, agentAction);
        else makeMove(agentState, agentAction, bestCoordinate);
    }

    /**
     * A function used to calculate the distance between two cells.
     *
     * @param startPosition: The startPosition
     * @param endPosition: The endPosition
     *
     * @return double distance variable
     */
    public static double calculateDistance(Coordinate startPosition, Coordinate endPosition) {
        int distanceX = Math.abs(startPosition.getX() - endPosition.getX());
        int distanceY = Math.abs(startPosition.getY() - endPosition.getY());
        int minDistance = Math.min(distanceX, distanceY);

        // Diagonal distance (minDistance) plus the rest (if distanceX or distanceY is larger than the other)
        return minDistance + Math.abs(distanceX - distanceY);
    }

    /**
     * A function to skip a turn.
     *
     * @param agentAction: The interface of actions for the agent
     */
    public static void skipTurn(AgentAction agentAction) {
        agentAction.skip();
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
        int relativePositionX = Integer.compare(dX, 0);
        int relativePositionY = Integer.compare(dY, 0);

        // Define the move coordinate

        return new Coordinate(relativePositionX, relativePositionY);
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

        if (pathCoordinate == null) return new Coordinate(agentState.getX(), agentState.getY());

        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int pathCoordinateX = pathCoordinate.getX();
        int pathCoordinateY = pathCoordinate.getY();

        // Calculate the difference between the positions
        int dX = pathCoordinateX - agentX;
        int dY = pathCoordinateY - agentY;

        // Calculate move
        int relativePositionX = Integer.compare(dX, 0);
        int relativePositionY = Integer.compare(dY, 0);

        // Define the move coordinate
        return new Coordinate(relativePositionX, relativePositionY);
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
            System.out.printf("%s: Moved to position (%d,%d)\n", agentState.getName(), agentNewX, agentNewY);
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
     * @param packetCoordinate The coordinate of the packet to pick up
     */
    public static void pickUpPacket(AgentState agentState, AgentAction agentAction, Coordinate packetCoordinate) {
        // Get the position
        int packetX = packetCoordinate.getX();
        int packetY = packetCoordinate.getY();

        // Perform pick up
        agentAction.pickPacket(packetX, packetY);

        // Inform
        System.out.printf("%s: Picked up packet %s\n", agentState.getName(), packetCoordinate);
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

        // Perform put down
        agentAction.putPacket(destinationX, destinationY);

        // Inform
        System.out.printf("%s: Put down packet %s\n", agentState.getName(), destinationCoordinate);
    }
}
