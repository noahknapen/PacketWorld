package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import util.assignments.general.GeneralUtils;
import java.io.IOException;

public class ChargingBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        try {
            GeneralUtils.handleChargingStations(agentState, agentCommunication);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        agentAction.skip();
    }
}