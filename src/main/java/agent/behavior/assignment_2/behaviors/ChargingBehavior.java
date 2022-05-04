package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import util.assignments.general.GeneralUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class ChargingBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        try {
            GeneralUtils.handleChargingStations(agentState, agentCommunication);
            GeneralUtils.handleDestinationLocations(agentState, agentCommunication);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        try {
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PREVIOUS_FIVE_MOVES, new ArrayList<Coordinate>()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        agentAction.skip();
    }
}