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
import util.assignments.targets.ChargingStation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
            updateChargingStation(agentState);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        agentAction.skip();
    }

    /////////////
    // METHODS //
    /////////////
    
    /**
     * Update the state of the chargingStation that is currently in use by the agent
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private void updateChargingStation(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get charging station position
        Coordinate charginStationCoordinate = new Coordinate(agentState.getX(), agentState.getY() - 1);
        
        // Get the current charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        for(ChargingStation chargingStation: chargingStations) {
            if(chargingStation.getCoordinate().equals(charginStationCoordinate)) {
                chargingStation.setBatteryOfUser(Optional.of(agentState.getBatteryState()));
                chargingStation.setInUse(true);
                return;
            }
        }
    } 
}