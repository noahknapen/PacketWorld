package agent.behavior.assignment_1_B;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.AgentComNecessities;
import util.AgentGeneralNecessities;
import util.graph.AgentGraphInteraction;

public class MoveRandomBehavior extends Behavior { 

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        AgentComNecessities.handleBatteryStations(agentState, agentCommunication);
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

        // Move randomly
        AgentGeneralNecessities.moveRandom(agentState, agentAction);

        AgentGraphInteraction.updateMappingMemory(agentState, null, null, agentPosition, null, null, null);
        AgentGraphInteraction.checkNodes(agentState);
    }
}