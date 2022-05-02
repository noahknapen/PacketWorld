package util.assignments.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import org.checkerframework.checker.units.qual.A;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

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
        // Get the last five positions
        ArrayList<Coordinate> lastPositions = new ArrayList<>();
        try {
            lastPositions = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PREVIOUS_FIVE_MOVES, Coordinate.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

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

                if (lastPositions.contains(new Coordinate(agentNewX, agentNewY))) continue;
                // Perform a step
                agentAction.step(agentNewX, agentNewY);

                // Update memory
                try {
                    updateLastFiveTurns(agentState, new Coordinate(agentNewX, agentNewY));
                } catch (IOException e) {
                    e.printStackTrace();
                }

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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate coordinate) throws JsonParseException, JsonMappingException, IOException {
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
        // If not in the graph, move closer to the position
        else {
            System.out.println("Test");
            ActionUtils.MoveRandomToPosition(agentState, agentAction, coordinate);
        }
    }

    private static void MoveRandomToPosition(AgentState agentState, AgentAction agentAction, Coordinate targetCoordinate) throws IOException {
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        ArrayList<Coordinate> lastPositions = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PREVIOUS_FIVE_MOVES, Coordinate.class);

        // Two variables for determining which is the best coordinate
        double minDistance = Double.MAX_VALUE;
        Coordinate bestCoordinate = agentPosition;

        for (Coordinate relativePosition : RELATIVE_POSITIONS) {
            // Calculate move
            CellPerception cellPerception = agentState.getPerception().getCellPerceptionOnRelPos(relativePosition.getX(), relativePosition.getY());

            //Check if cell is walkable
            if (cellPerception == null || !cellPerception.isWalkable()) continue;

            // Create a variable of the position to try
            int newPositionX = agentState.getX() + relativePosition.getX();
            int newPositionY = agentState.getY() + relativePosition.getY();
            Coordinate newPosition = new Coordinate(newPositionX, newPositionY);

            // Check if cell is already walked on last five turns
            if (lastPositions.contains(newPosition)) continue;

            // Check if cell is a better option
            if (ActionUtils.calculateDistance(newPosition, targetCoordinate) > minDistance) continue;

            // It is a better option so change the min distance and the relative position
            minDistance = ActionUtils.calculateDistance(newPosition, targetCoordinate);
            bestCoordinate = relativePosition;
        }

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
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private static Coordinate calculateMoveAStar(AgentState agentState,  Coordinate target) throws JsonParseException, JsonMappingException, IOException {
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

            // Update memory
            try {
            updateLastFiveTurns(agentState, new Coordinate(agentNewX, agentNewY));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Inform
            String message = String.format("%s: Moved to position (%d,%d)", agentState.getName(), agentNewX, agentNewY);
            System.out.println(message);
        }
        else ActionUtils.moveRandomly(agentState, agentAction);
    }

    private static void updateLastFiveTurns(AgentState agentState, Coordinate move) throws IOException {
        ArrayList<Coordinate> lastPositions = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PREVIOUS_FIVE_MOVES, Coordinate.class);

        if (lastPositions.size() != 0) lastPositions.remove(0);

        lastPositions.add(move);
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PREVIOUS_FIVE_MOVES, lastPositions));
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
