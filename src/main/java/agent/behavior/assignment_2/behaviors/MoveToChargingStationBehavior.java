package agent.behavior.assignment_2.behaviors;

import agent.AgentAction;
import agent.AgentCommunication;
import agent.AgentState;
import agent.behavior.Behavior;
import environment.Coordinate;
import environment.Perception;
import util.assignments.general.ActionUtils;
import util.assignments.general.CommunicationUtils;
import util.assignments.general.GeneralUtils;
import util.assignments.graph.GraphUtils;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;

import java.util.ArrayList;
import java.util.Optional;

public class MoveToChargingStationBehavior extends Behavior {

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public void communicate(AgentState agentState, AgentCommunication agentCommunication) {
        // Communicate the charging stations with all the other agents
        GeneralUtils.handleChargingStationsCommunication(agentState, agentCommunication);

        // Communicate the destination locations with agents in perception
        GeneralUtils.handleDestinationsCommunication(agentState, agentCommunication);

        // Communicate the graph with agents in perception
        GeneralUtils.handleGraphCommunication(agentState, agentCommunication);

        // If energy lower than a threshold, send emergency message
        sendEmergencyMessage(agentState, agentCommunication);
    }

    @Override
    public void act(AgentState agentState, AgentAction agentAction) {
        // Check the perception of the agent
        GeneralUtils.checkPerception(agentState);

        // Build the graph
        GraphUtils.build(agentState);

        // Move the agent to the target
        moveToChargingStation(agentState, agentAction);
    }

    /**
     * A function to find the closest chargingStation and move towards it.
     *
     * @param agentState: The state of the agent
     * @param agentAction: The action interface of the agent
     */
    private void moveToChargingStation(AgentState agentState, AgentAction agentAction) {
        // Retrieve the charging stations from memory
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Initialise some variables to keep track of the best option
        double minDistance = Double.MAX_VALUE;
        Coordinate bestPosition = null;
        ChargingStation bestStation = null;

        // Create the agent position
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());

        // Iterate through all the stations
        for (ChargingStation station : discoveredChargingStations) {

            // Find coordinates of charging station
            int batteryX = station.getCoordinate().getX();
            int batteryY = station.getCoordinate().getY() - 1;
            Coordinate chargingCoordinates = new Coordinate(batteryX, batteryY);

            // Calculate the distance between current position and the current charging station
            int currentDistance = Perception.distance(agentPosition.getX(), agentPosition.getY(), chargingCoordinates.getX(), chargingCoordinates.getY());

            // Guard clause to ensure the charging station is closer than the previous one
            if (currentDistance > minDistance) continue;

            // Set the variables to the new best option
            minDistance = currentDistance;
            bestPosition = chargingCoordinates;
            bestStation = station;

        }

        // If the agent is close to the chargingStation, but it is in use skip a turn to conserve energy.
        if (bestPosition != null) {
            // Calculate distance to charging station
            int distance = Perception.distance(agentPosition.getX(), agentPosition.getY(), bestPosition.getX(), bestPosition.getY());

            // Skip to conserve energy if station in use, and we are too close
            if (distance <= 5 & bestStation.isInUse()) ActionUtils.skipTurn(agentAction);
            else ActionUtils.moveToPosition(agentState, agentAction, bestPosition);
        } else {
            // Move random if the best position is null
            ActionUtils.moveRandomly(agentState, agentAction);
        }
    }

    /**
     * A function that returns whether the charging station will be empty upon arrival
     *
     * @param station: The charging station
     *
     * @return True if the station is empty upon arrival, false otherwise.
     */
    private boolean emptyWhenWeArrive(AgentState agentState, ChargingStation station) {
        // Retrieve the battery level of the user on the station
        Optional<Integer> batteryLevelUserOnStation = station.getBatteryOfUser();

        // If the station isn't in use it is currently projected free for when we arrive.
        if (!station.isInUse() || batteryLevelUserOnStation.isEmpty()) return true;

        // Calculate the turns away from the station and the turns the station is in use
        Coordinate agentPosition = new Coordinate(agentState.getX(), agentState.getY());
        Coordinate stationPosition = station.getCoordinate();
        double turnsAwayFromStation = Perception.distance(agentPosition.getX(), agentPosition.getY(), stationPosition.getX(), stationPosition.getY());
        double turnsTheStationIsInUse = (environment.EnergyValues.BATTERY_MAX - batteryLevelUserOnStation.get()) / 100.0;

        // If the turns away from the station is more than the turns in use, the station will be empty upon arrival
        return Math.ceil(turnsAwayFromStation) >= Math.ceil(turnsTheStationIsInUse);
    }


    /**
     * A function that sends an emergency message to the agent that is using the charging station to tell them there
     * battery is extremely low.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     */
    private void sendEmergencyMessage(AgentState agentState, AgentCommunication agentCommunication) {
        // Check if the battery level is low enough to send emergency notification
        if (75 < agentState.getBatteryState()) return;
        if (0 == agentState.getBatteryState()) return;

        // Construct message
        String msg = "true";
        String type = "boolean";
        boolean sent = CommunicationUtils.sendEmergencyMessage(agentState, agentCommunication, msg, type);

        // Inform dev
        if (sent) System.out.printf("%s: Message sent\n", agentState.getName());
    }

}