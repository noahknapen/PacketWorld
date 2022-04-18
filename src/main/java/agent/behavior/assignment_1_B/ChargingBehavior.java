package agent.behavior.assignment_1_B;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import util.AgentComNecessities;

public class ChargingBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        AgentComNecessities.handleBatteryStations(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        agentAction.skip();
    }
}