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
        if(positionInPerception(agentState, coordinate)) {
            moveToPositionInPerception(agentState, agentAction, coordinate);
        }
        else {
            // TODO

        }
    }

    /**
     * A function to make the agent move to a position in the perception
     * 
     * @param agentState
     * @param agentAction
     * @param coordinate
     */
    private static void moveToPositionInPerception(AgentState agentState, AgentAction agentAction, Coordinate coordinate) {
        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the positions
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Calculate the difference between the positions
        int dX = coordinateX - agentX;
        int dY = coordinateY - agentY;

        // Calculate move
        int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
        int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);

        // Get corresponding cell
        CellPerception cellPerception = agentPerception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

        // Check if the cell is walkable
        if (cellPerception != null && cellPerception.isWalkable()) {
            // Calculate the move
            int agentNewX = agentX + relativePositionX;
            int agentNewY = agentY + relativePositionY;

            // Perform a step
            agentAction.step(agentNewX, agentNewY);

            // Inform
            String message = String.format("%s: Moved to position in perception %s", agentState.getName(), coordinate.toString());
            System.out.println(message);
        }
        else ActionUtils.moveRandomly(agentState, agentAction);
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

        ////////////////////////////
        // POSITION IN PERCEPTION //
        ////////////////////////////
    
    /**
     * A function to know if a specific position is in the perception of the agent
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the perception of the agent, otherwise false
     */
    public static boolean positionInPerception(AgentState agentState, Coordinate coordinate) {
        // Get the position
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                CellPerception cellPerception = agentPerception.getCellAt(x,y);

                // Check if the cell is null and continue with the next cell if so
                if(cellPerception == null) continue;

                // Get the position of the cell
                int cellX = cellPerception.getX();
                int cellY = cellPerception.getY();
                
                // Check if the positions correpond
                if(cellX == coordinateX && cellY == coordinateY) 
                    return true;
            }
        }

        return false;
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
