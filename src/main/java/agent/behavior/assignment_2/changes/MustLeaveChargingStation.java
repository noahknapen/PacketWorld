package agent.behavior.assignment_2.changes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import agent.AgentState;
import agent.behavior.BehaviorChange;
import environment.Coordinate;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

public class MustLeaveChargingStation extends BehaviorChange {
    private boolean hasEnoughBattery = false;

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void updateChange() {
        AgentState agentState = this.getAgentState();
        hasEnoughBattery =  this.getAgentState().getBatteryState() >= 900;

        if(hasEnoughBattery) {
            try {
                updateCharginStation(agentState);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean isSatisfied() {
        return hasEnoughBattery;
    }

    /////////////
    // METHODS //
    /////////////
    
    /**
     * Update the state of the chargingStation that is currently in use by the agent
     * 
     * @param agentState The current state of the agent
     * 
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    private void updateCharginStation(AgentState agentState) throws JsonParseException, JsonMappingException, IOException {
        // Get charging station position
        Coordinate charginStationCoordinate = new Coordinate(agentState.getX(), agentState.getY() - 1);
        
        // Get the current charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        for(ChargingStation chargingStation: chargingStations) {
            if(chargingStation.getCoordinate().equals(charginStationCoordinate)) { 
                chargingStation.setBatteryOfUser(Optional.empty());
                chargingStation.setInUse(false);
                return;
            }
        }
    } 

}
