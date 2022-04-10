package agent.behavior.assignment_1_B;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import util.AgentGeneralNecessities;
import util.MemoryKeys;
import util.graph.AgentGraphInteraction;
import util.graph.Graph;
import util.task.Task;

public class MoveToDestinationBehavior extends Behavior {

    final ArrayList<Coordinate> RELATIVE_POSITIONS = new ArrayList<Coordinate>(List.of(
        new Coordinate(1, 1), 
        new Coordinate(-1, -1),
        new Coordinate(1, 0), 
        new Coordinate(-1, 0),
        new Coordinate(0, 1), 
        new Coordinate(0, -1),
        new Coordinate(1, -1), 
        new Coordinate(-1, 1)
    ));

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // TODO Auto-generated method stub
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {

        // Update agents previous position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);

        // Handle graph
        AgentGraphInteraction.handleGraph(agentState);

        // Check perception
        AgentGeneralNecessities.checkPerception(agentState);

        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();
        // Check if task exists in memory
        if(memoryFragments.contains(MemoryKeys.TASK)) {
            // Retrieve task and position
            String taskString = agentState.getMemoryFragment(MemoryKeys.TASK);
            Task task = Task.fromJson(taskString);
            Coordinate position = task.getDestination().getCoordinate();

            // Move to position
            moveToPosition(agentState, agentAction, position);
        }
        else AgentGeneralNecessities.moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null);
    }


    /////////////
    // METHODS //
    /////////////
 
    /**
     * Move towards a specific position
     *
     * @param agentState Current state of agent
     * @param agentAction Perform an action with agent
     * @param position Position to move towards
     */
    private void moveToPosition(AgentState agentState, AgentAction agentAction, Coordinate position) {
        // Retrieve positions
        Perception perception = agentState.getPerception();
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        Coordinate agentPosition = new Coordinate(agentX, agentY);
        int positionX = position.getX();
        int positionY = position.getY();

        // Retrieve path
        List<Coordinate> path = getPath(agentState);

        // Check if position is in current perception
        if(positionInPerception(agentState, position)) {
            // Calculate move
            int dX = positionX - agentX;
            int dY = positionY - agentY;
            int relativePositionX = (dX > 0) ? 1 : ((dX < 0) ? -1 : 0);
            int relativePositionY = (dY > 0) ? 1 : ((dY < 0) ? -1 : 0);
            CellPerception cellPerception = perception.getCellPerceptionOnRelPos(relativePositionX, relativePositionY);

            // Check if cell is walkable
            if (cellPerception != null && cellPerception.isWalkable()) {
                int newPositionX = agentX + relativePositionX;
                int newPositionY = agentY + relativePositionY;

                // Perform a step
                agentAction.step(newPositionX, newPositionY);

            }
            else AgentGeneralNecessities.moveRandom(agentState, agentAction);

            path.clear();
            AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, null);
        }
        else {
            Graph graph = AgentGraphInteraction.getGraph(agentState);
            Coordinate shouldBeHerePosition = getShouldBeHerePosition(agentState);

            // If path exists -> Just follow the path.
            if (!path.isEmpty()) {

                // If previous movement failed for some reason -> Try again.
                if (!agentPosition.equals(shouldBeHerePosition)) {
                    moveToPosition(agentState, agentAction, shouldBeHerePosition);
                    return;
                }

                Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                shouldBeHerePosition = nextCoordinate;

                agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
            }

            // If agent position outside the graph -> Move to the closest node first.
            else if (!graph.nodeExists(agentPosition))
            {
                Coordinate closestNodeCoordinate = graph.closestFreeNodeCoordinate(agentState.getPerception(), agentPosition);
                moveToPosition(agentState, agentAction, closestNodeCoordinate);
            }

            // Search for path from current position to the desired position.
            else
            {
                // Perform Dijkstra's algorithm
                path = graph.doSearch(agentPosition, position);

                if (!path.isEmpty())
                {
                    Coordinate nextCoordinate = path.remove(0); // TODO: Maybe path should not be linked list. (Stack?)
                    shouldBeHerePosition = nextCoordinate;
                    agentAction.step(nextCoordinate.getX(), nextCoordinate.getY());

                    AgentGraphInteraction.updateMappingMemory(agentState, null, path, null, null, shouldBeHerePosition);
                }
                else
                {
                    AgentGeneralNecessities.moveRandom(agentState, agentAction);
                }
            }
        }
    }

    
    /**
     * Check if position is in current perception
     *
     * @param agentState Current state of agent
     * @param position Position to check
     * @return True if position is in current perception
     */
    private boolean positionInPerception(AgentState agentState, Coordinate position) {
        // Retrieve position
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

    /**
     * Retrieve path from memory
     * Create path if not yet created
     * 
     * @param agentState Current state of agent
     * @return Path: List of coordinate
     */ 
    private List<Coordinate> getPath(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        Gson gson = new Gson();
        // Check if path exists in memory
        if(memoryFragments.contains(MemoryKeys.PATH)) {
            // Retrieve path
            String pathString = agentState.getMemoryFragment(MemoryKeys.PATH);
            return gson.fromJson(pathString, new TypeToken<List<Coordinate>>(){}.getType());
        }
        else {
            // Create path
            List<Coordinate> path = new LinkedList<>();

            // Add path to memory
            String pathString = gson.toJson(path);
            agentState.addMemoryFragment(MemoryKeys.PATH, pathString);

            return path;
        }
    }  

    /**
     * Retrieve should be here position from memory
     * 
     * @param agentState Current state of agent
     * @return Should be here position
     */ 
    private Coordinate getShouldBeHerePosition(AgentState agentState) {
        // Retrieve memory of agent
        Set<String> memoryFragments = agentState.getMemoryFragmentKeys();

        // Check if should be here position exists in memory
        if(memoryFragments.contains(MemoryKeys.SHOULD_BE_HERE_POSITION)) {
            // Retrieve should be here position
            Gson gson = new Gson();
            String shouldBeHereString = agentState.getMemoryFragment(MemoryKeys.SHOULD_BE_HERE_POSITION);
            return gson.fromJson(shouldBeHereString, Coordinate.class);
        }
        else return null;
    }
}