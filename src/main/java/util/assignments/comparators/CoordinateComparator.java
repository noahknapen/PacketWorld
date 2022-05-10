package util.assignments.comparators;

import java.util.Comparator;

import agent.AgentState;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;

/**
 * A class implementing a comparator for coordinates
 * Coordinates are sorted based on:
 * - Distance to a target coordinate
 */
public class CoordinateComparator implements Comparator<Coordinate> {

    private AgentState agentState;
    private Coordinate targetCoordinate;

    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public CoordinateComparator(AgentState agentState, Coordinate targetCoordinate) {
        this.setAgentState(agentState);
        this.setTargetCoordinate(targetCoordinate);
    }

    /////////////
    // SETTERS //
    /////////////

    private void setAgentState(AgentState agentState) {
        this.agentState = agentState;
    }
    
    private void setTargetCoordinate(Coordinate targetCoordinate) {
        this.targetCoordinate = targetCoordinate;
    }

    ///////////////
    // OVERRIDES //
    ///////////////
    
    /**
     * Compare two coordinates
     * 
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     * @return 0 if both coordinates are equivalent, 1 if the first coordinate has priority, -1 if the second coordinate has priority
     */
    @Override
    public int compare(Coordinate coordinate1, Coordinate coordinate2) {
        // Get the position of the agent and the target
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int targetX = targetCoordinate.getX();
        int targetY = targetCoordinate.getY();

        // Calculate the coordinate of the move
        int moveX = agentX + targetX;
        int moveY = agentY + targetY;
        Coordinate moveCoordinate = new Coordinate(moveX, moveY);

        // Calculate the distances of both coordinates
        double distanceCoordinate1 = GeneralUtils.calculateEuclideanDistance(coordinate1, moveCoordinate);
        double distanceCoordinate2 = GeneralUtils.calculateEuclideanDistance(coordinate2, moveCoordinate);

        // Compare the distances
        if(distanceCoordinate1 == distanceCoordinate2) return 0;
        else if(distanceCoordinate1 < distanceCoordinate2) return 1;
        else return -1;
    }
}
