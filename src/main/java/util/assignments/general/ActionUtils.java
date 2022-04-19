package util.assignments.general;

import java.util.ArrayList;
import java.util.List;

import agent.AgentAction;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;

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

        ///////////////
        // TO TARGET //
        ///////////////

    /**
     * A function to make the agent move to a coordinate
     * 
     * @param agentAction Perform an action with the agent
     * @param coordinate The coordinate to move to
     */
    public static void moveToCoordinate(AgentAction agentAction, Coordinate coordinate) {
        agentAction.skip();
    }

        //////////////////////
        // POSITION REACHED //
        //////////////////////
    
    /**
     * A function to know if the agent has reached the position
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to reach
     * @return True is the agent is next to the position, otherwise false
     */
    public static boolean hasReachedPosition(AgentState agentState, Coordinate coordinate) {
        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Calculate the difference between the positions
        int dX = Math.abs(agentX - coordinateX);
        int dY = Math.abs(agentY - coordinateY);

        // Return true if the distance is less than 1 for both axes
        return (dX <= 1) && (dY <= 1);
    }   

    ////////////
    // PACKET //
    ////////////

    /**
     * A function to perform the pick up
     * 
     * @param agentAction Perform an action with the agent
     * @param packeCoordinate The coordinate of the packet to pick up
     */
    public static void pickUpPacket(AgentAction agentAction, Coordinate packetCoordinate) {
        int packetX = packetCoordinate.getX();
        int packetY = packetCoordinate.getY();

        agentAction.pickPacket(packetX, packetY);
    }

    /**
     * A function to perform the put down
     * 
     * @param agentAction Perform an action with the agent
     * @param destinationCoordinate The coordinate of the destination where to put down the packet
     */
    public static void putDownPacket(AgentAction agentAction, Coordinate destinationCoordinate) {
        int destinationX = destinationCoordinate.getX();
        int destinationY = destinationCoordinate.getY();

        agentAction.putPacket(destinationX, destinationY);
    }
}
