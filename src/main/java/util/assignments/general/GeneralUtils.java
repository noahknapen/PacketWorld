package util.assignments.general;

import java.awt.*;
import java.util.*;

import java.util.List;

import agent.AgentCommunication;
import agent.AgentState;
import environment.Coordinate;
import environment.Perception;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.task.Task;

/**
 * A class that implements general functions
 */
public class GeneralUtils {

    // A static data member holding the cost of a walk without a packet
    public static final int COST_WALK_WITHOUT_PACKET = 10;
    // A static data member holding the cost of a walk with a packet
    public static final int COST_WALK_WITH_PACKET = 25;
    // A static data member holding if messages should be printed in the output
    public static final boolean PRINT = false;

    ////////////////
    // PERCEPTION //
    ////////////////

    /**
     * A function that adds a charging station to the memory of the agent
     *
     * @param agentState                The current state of the agent
     * @param chargingStationCoordinate The coordinates of the charging station
     */
    public static void addChargingStation(AgentState agentState, Coordinate chargingStationCoordinate) {
        // Retrieve the memory fragment
        ArrayList<ChargingStation> discoveredChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Create the corresponding charging station
        ChargingStation chargingStation = new ChargingStation(chargingStationCoordinate);

        // Check if the charging station was already discovered and continue with next cell if so
        if (discoveredChargingStations.contains(chargingStation)) return;

        // Add the charging station to the list of discovered charging stations
        discoveredChargingStations.add(chargingStation);

        // Inform
        if (GeneralUtils.PRINT)
            System.out.printf("%s: Discovered a new charging station (%s) [%s]\n", agentState.getName(), chargingStation, discoveredChargingStations.size());

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, discoveredChargingStations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /////////////////////////
    // INFORMATION SHARING //
    /////////////////////////

    /////////////
    // GENERAL //
    /////////////

    /**
     * A function that is used to communicate information about the charging stations.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    public static void handleChargingStationsCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Get if the list of discovered charging stations was updated
        boolean updatedStations = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_STATIONS, Boolean.class));

        // Check if the list of discovered charging stations was updated
        if (updatedStations) {
            // Share information about the charging stations
            shareChargingStations(agentState, agentCommunication);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, false));
        } else {
            // Look at messages, If we shared something the others can not have changed something or give priority to true statements
            updateChargingStations(agentState, agentCommunication);
        }
    }

    /**
     * A function that is used to communicate information about the graph.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    public static void handleGraphCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Share the graph
        shareGraph(agentState, agentCommunication);

        // Update graph
        updateGraph(agentState, agentCommunication);
    }

    ///////////
    // SHARE //
    ///////////

    /**
     * A function that is used to share the charging stations with other agents.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void shareChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Broadcast the list of discovered charging stations
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS);
    }

    /**
     * A function that is used to share the graph with other agents.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void shareGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Send messages with the graph
        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.GRAPH);
    }

    ////////////
    // UPDATE //
    ////////////

    /**
     * A function that updates the list of discovered charging stations with the information received from other agents.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void updateChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current charging stations
        ArrayList<ChargingStation> currentChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Get the updated charging stations
        ArrayList<ChargingStation> updatedChargingStations = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.DISCOVERED_CHARGING_STATIONS, ChargingStation.class);

        // Loop over updated charging stations
        for (ChargingStation updatedChargingStation : updatedChargingStations) {
            // Check if the charging station is not included in the current list
            if (!currentChargingStations.contains(updatedChargingStation)) {
                // Add the new charging station to the charging stations
                currentChargingStations.add(updatedChargingStation);

                // Inform
                if (GeneralUtils.PRINT)
                    System.out.printf("%s: Added a new charging station from communication (%s) [%s]\n", agentState.getName(), updatedChargingStation, currentChargingStations.size());

                continue;
            }

            // Loop over current charging stations
            for (ChargingStation currentChargingStation : currentChargingStations) {
                // Check if charging stations correspond
                if (currentChargingStation.equals(updatedChargingStation)) {
                    // Update the current charging station if needed
                    currentChargingStation.setInUse(updatedChargingStation.isInUse());
                    currentChargingStation.setBatteryOfUser(updatedChargingStation.getBatteryOfUser());

                    // Inform
                    if (GeneralUtils.PRINT)
                        System.out.printf("%s: Updated a known charging station from communication (%s)\n", agentState.getName(), currentChargingStation);
                }
            }
        }

        // Update the current charging stations
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.DISCOVERED_CHARGING_STATIONS, currentChargingStations));
    }

    /**
     * A function that updates the graph with the information received from other agents.
     *
     * @param agentState         The current state of the agent
     * @param agentCommunication The interface for communication
     */
    private static void updateGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the updated graph
        HashMap<String, Graph> graphMessages = CommunicationUtils.getObjectFromMails(agentCommunication, MemoryKeys.GRAPH, Graph.class);

        // No messages about the graph received
        if (graphMessages == null) return;

        // If the message comes from the agent return
        if (graphMessages.containsKey(agentState.getName())) return;

        // Loop over the received messages
        for (String sender : graphMessages.keySet()) {
            // Get an update of the graphs
            Graph updatedGraph = graphMessages.get(sender);

            // Update the current graph
            GraphUtils.update(agentState, updatedGraph);

            // Inform
            if (GeneralUtils.PRINT)
                System.out.printf("%s: Updated the graph from communication\n", agentState.getName());
        }
    }


    ///////////
    // UTILS //
    ///////////

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


    /**
     * A function to know if a specific position is in the graph
     *
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the graph, otherwise false
     */
    public static boolean positionInGraph(AgentState agentState, Coordinate coordinate) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if the graph is null and return false turn if so
        if (graph == null) return false;

        // Get the graph map
        Map<Node, List<Node>> map = graph.getMap();

        // Loop over graph nodes
        for (Node node : map.keySet()) {
            // Get the position of the node
            Coordinate nodeCoordinate = node.getCoordinate();

            // Check if coordinates correspond
            if (nodeCoordinate.equals(coordinate)) return true;

        }

        return false;
    }

    /**
     * Checks if the task can reach its destination (does not have to be a packet destination)
     * @param agentState The agent state
     * @param task The task
     * @return True if destination can be reached
     */
    public static boolean canReachDestination(AgentState agentState, Task task) {

        ArrayList<Destination> discoveredDestinations = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Destination.class);

        // Loop over the discovered destinations
        for (Destination candidateDestination : discoveredDestinations) {
            // Get the color of the candidate destination
            Color candidateDestinationColor = candidateDestination.getColor();

            // Check if the colors correspond
            if (task.getPacket().getRgbColor() != candidateDestinationColor.getRGB()) continue;

            // If the agent hasn't got enough energy to work on it, it will not start the work
            if (!GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, task.getPacket(), candidateDestination))
                continue;

            // Check if path exists to destination
            ArrayList<Node> destinationPath = GraphUtils.performAStarSearch(agentState, candidateDestination.getCoordinate(), false);

            if (destinationPath != null) {
                Coordinate destinationCoordinate = destinationPath.get(destinationPath.size()-1).getCoordinate();
                Destination destination = new Destination(destinationCoordinate, task.getPacket().getRgbColor());
                task.setDestination(destination);
                MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task));
                return true;
            }
        }

        return false;
    }

    /**
     * A function that determines whether the agent has enough energy left to pick up the given packet en deliver it
     * to the given destination. The cost is based on where the agent is currently standing.
     *
     * @param agentState:  The state of the agent
     * @param packet:      The packet it has to check
     * @param destination: The destination the packet has to go to
     * @return True if enough energy to perform the task, false otherwise.
     */
    public static boolean hasEnoughBatteryToCompleteTask(AgentState agentState, Packet packet, Destination destination) {
        // First calculate the power to go to the packet location
        Coordinate packetPosition = packet.getCoordinate();
        int cellsToWalk1 = Perception.distance(agentState.getX(), agentState.getY(), packetPosition.getX(), packetPosition.getY());
        double powerToGoToPacket = cellsToWalk1 * GeneralUtils.COST_WALK_WITHOUT_PACKET;

        // Second calculate the power to go from packet to destination
        Coordinate destinationPosition = destination.getCoordinate();
        int cellsToWalk2 = Perception.distance(packetPosition.getX(), packetPosition.getY(), destinationPosition.getX(), destinationPosition.getY());
        double powerToGoToDestination = cellsToWalk2 * GeneralUtils.COST_WALK_WITH_PACKET;

        // See if there is enough power to complete the task and have an extra buffer
        return agentState.getBatteryState() > (powerToGoToDestination + powerToGoToPacket + 150);
    }

    /**
     * A function to calculate the Euclidean distance between two coordinates
     *
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     */
    public static double calculateEuclideanDistance(Coordinate coordinate1, Coordinate coordinate2) {
        // Get the positions
        int coordinate1X = coordinate1.getX();
        int coordinate1Y = coordinate1.getY();
        int coordinate2X = coordinate2.getX();
        int coordinate2Y = coordinate2.getY();

        // Calculate the distance
        return Math.sqrt(((coordinate2Y - coordinate1Y) * (coordinate2Y - coordinate1Y)) + ((coordinate2X - coordinate1X) * (coordinate2X - coordinate1X)));
    }

    /**
     * Handles communication of priority tasks (packets that blocks other things)
     * @param agentState The agent state
     * @param agentCommunication The agent communication
     */
    public static void handlePriorityTaskCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Share your priority tasks
        sharePriorityTasks(agentState, agentCommunication);

        // Get priorityTasks from other agents
        updatePriorityTasks(agentState, agentCommunication);
    }

    /**
     * Shares the list of priority tasks that the agent itself can't handle
     * @param agentState The agent state
     * @param agentCommunication The agent communication
     */
    private static void sharePriorityTasks(AgentState agentState, AgentCommunication agentCommunication) {
        // Send message
        if (MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS_SEND, Task.class).size() == 0) return;

        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.PRIORITY_TASKS_SEND);
    }

    /**
     * Receives priority tasks from other agents and updates its own
     * priority task list of the agent can handle the task.
     * @param agentState The agent state
     * @param agentCommunication The agent communication
     */
    private static void updatePriorityTasks(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the priority tasks
        ArrayList<Task> priorityTasks = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS, Task.class);

        // Get the updated priority tasks
        ArrayList<Task> receivedPriorityTasks = CommunicationUtils.getListFromMails(agentState, agentCommunication, MemoryKeys.PRIORITY_TASKS_SEND, Task.class);

        // Loop over updated priority tasks
        for (Task task : receivedPriorityTasks) {

            // If task packet has same color as agent
            if (!priorityTasks.contains(task) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() == task.getPacket().getRgbColor()) {
                priorityTasks.add(task);
            }
        }

        // Update memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PRIORITY_TASKS, priorityTasks));
    }
}
