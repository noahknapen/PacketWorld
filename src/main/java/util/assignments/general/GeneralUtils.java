package util.assignments.general;

import java.awt.Color;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import agent.AgentCommunication;
import agent.AgentState;
import environment.CellPerception;
import environment.Coordinate;
import environment.Perception;
import environment.world.destination.DestinationRep;
import environment.world.packet.PacketRep;
import util.assignments.graph.Graph;
import util.assignments.graph.GraphUtils;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.targets.Target;
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
     * Discover a charging station
     *
     * @param agentState The current state of the agent
     * @param chargingStationCoordinate The coordinates of the charging station
     */
    public static void discoverChargingStation(AgentState agentState, Coordinate chargingStationCoordinate) {
        // Get the list of charging stations
        ArrayList<ChargingStation> chargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Create a charging station
        ChargingStation chargingStation = new ChargingStation(chargingStationCoordinate);

        // Check if the charging station was already discovered and return if so
        if(chargingStations.contains(chargingStation)) return;

        // Add the charging station to the charging stations
        chargingStations.add(chargingStation);

        // Inform
        if(GeneralUtils.PRINT) {
            System.out.printf("%s: Discovered a new charging station (%s) [%s]\n", agentState.getName(), chargingStation, chargingStations.size());
        } 

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.CHARGING_STATIONS, chargingStations, MemoryKeys.UPDATED_STATIONS, true));
    }

    /////////////////////////
    // INFORMATION SHARING //
    /////////////////////////

    // GENERAL //

    /**
     * Handle the communication of the charging stations between agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    public static void handleChargingStationsCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Get if the charging stations were updated
        boolean chargingStationsUpdated = Boolean.TRUE.equals(MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.UPDATED_STATIONS, Boolean.class));

        // Check if the charging stations were updated
        if(chargingStationsUpdated) {
            // Share the charging stations
            GeneralUtils.shareChargingStations(agentState, agentCommunication);

            // Update the memory
            MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.UPDATED_STATIONS, false));
        }

        // Update the charging stations
        GeneralUtils.updateChargingStations(agentState, agentCommunication);
    }

    /**
     * Handle the communication of the graph between agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    public static void handleGraphCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Share the graph
        GeneralUtils.shareGraph(agentState, agentCommunication);

        // Update the graph
        GeneralUtils.updateGraph(agentState, agentCommunication);
    }

    /**
     * Handle the communication of priority tasks between agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    public static void handlePriorityTaskCommunication(AgentState agentState, AgentCommunication agentCommunication) {
        // Share the priority tasks
        sharePriorityTasks(agentState, agentCommunication);

        // Update the priority tasks
        updatePriorityTasks(agentState, agentCommunication);
    }

    // SHARE //

    /**
     * Share the charging stations with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void shareChargingStations(AgentState agentState, AgentCommunication agentCommunication) {
        // Broadcast the charging stations
        CommunicationUtils.broadcastMemoryFragment(agentState, agentCommunication, MemoryKeys.CHARGING_STATIONS);
    }

    /**
     * Share the graph with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void shareGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Send the graph to agents in the perception of the agent
        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.GRAPH);
    }

    /**
     * Share the priority tasks with other agents
     * @param agentState The agent state
     * @param agentCommunication The agent communication
     */
    private static void sharePriorityTasks(AgentState agentState, AgentCommunication agentCommunication) {
        // Send the priority tasks to agents in the perception of the agent
        CommunicationUtils.sendMemoryFragment(agentState, agentCommunication, MemoryKeys.PRIORITY_TASKS_SEND);
    }

    // UPDATE //

    /**
     * Update the charging stations based on communication with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void updateChargingStations(AgentState agentState, AgentCommunication agentCommunication) {    
        // Get the current charging stations
        ArrayList<ChargingStation> currentChargingStations = MemoryUtils.getListFromMemory(agentState, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Get the messages
        HashMap<String, ArrayList<ChargingStation>> chargingStationMessages = CommunicationUtils.getObjectListsFromMails(agentState, agentCommunication, MemoryKeys.CHARGING_STATIONS, ChargingStation.class);

        // Check if no charging station messages were received and return if so
        if (chargingStationMessages.size() == 0) {
            return;
        }

        // Loop over all charging station messages
        for(String sender: chargingStationMessages.keySet()) {
            // Check if sender equals the agent and continue with next charging station message if so
            if(sender.equals(agentState.getName())) {
                continue;
            }

            // Get the update charging stations of the charging station message
            ArrayList<ChargingStation> updatedChargingStations = chargingStationMessages.get(sender);

            // Loop over updated charging stations
            for(ChargingStation updatedChargingStation: updatedChargingStations) {
                // Check if the current charging stations do not contain the updated charging station
                if(!currentChargingStations.contains(updatedChargingStation)) {
                    // Add the updated charging station to the current charging stations
                    currentChargingStations.add(updatedChargingStation);

                    // Inform
                    if (GeneralUtils.PRINT) {
                        System.out.printf("%s: Added a new charging station from communication (%s) [%s]\n", agentState.getName(), updatedChargingStation, currentChargingStations.size());
                    }

                    continue;
                }            

                // Loop over current charging stations
                for(ChargingStation currentChargingStation: currentChargingStations) {
                    // Check if the current charging station equals the updated charging station
                    if(currentChargingStation.equals(updatedChargingStation)) {
                        // Check if the current charging station is not in use
                        // The current charging station should only be set to in use if it is not already
                        if(!currentChargingStation.isInUse()) {
                            currentChargingStation.setInUse(updatedChargingStation.isInUse());
                            currentChargingStation.setBatteryOfUser(updatedChargingStation.getBatteryOfUser());

                            // Inform
                            if(GeneralUtils.PRINT) {
                                System.out.printf("%s: Updated a known charging station from communication (%s)\n", agentState.getName(), currentChargingStation);
                            }
                        }
                    }
                }
            }
        }
        
        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.CHARGING_STATIONS, currentChargingStations));
    }

    /**
     * Update the graph based on communication with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void updateGraph(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the messages
        HashMap<String, Graph> graphMessages = CommunicationUtils.getObjectsFromMails(agentCommunication, MemoryKeys.GRAPH, Graph.class);

        // Check if no graph messages were received and return if so
        if (graphMessages.size() == 0) {
            return;
        }

        // Loop over all graph messages
        for(String sender: graphMessages.keySet()) {
            // Check if sender equals the agent and continue with next graph message if so
            if(sender.equals(agentState.getName())) {
                continue;
            }

            // Get the update graph of the graph message
            Graph updatedGraph = graphMessages.get(sender);

            // Update the current graph
            GraphUtils.update(agentState, updatedGraph);

            // Inform
            if (GeneralUtils.PRINT) {
                System.out.printf("%s: Updated the graph from communication\n", agentState.getName());
            }
        }
    }

    /**
     * Update the priority tasks based on communication with other agents
     *
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     */
    private static void updatePriorityTasks(AgentState agentState, AgentCommunication agentCommunication) {
        // Get the current priority tasks
        ArrayList<Task> currentPriorityTasks = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS, Task.class);

        // Get the messages
        HashMap<String, ArrayList<Task>> priorityTasksMessages = CommunicationUtils.getObjectListsFromMails(agentState, agentCommunication, MemoryKeys.PRIORITY_TASKS_SEND, Task.class);

        // Check if no priority tasks messages were received and return if so
        if (priorityTasksMessages.size() == 0) {
            return;
        }

        // Loop over all priority tasks messages
        for (String sender: priorityTasksMessages.keySet()) {
            // Check if sender equals the agent and continue with next priority tasks message if so
            if(sender.equals(agentState.getName())) {
                continue;
            }

            // Get the priority tasks from the priority tasks message
            ArrayList<Task> updatedPriorityTasks = priorityTasksMessages.get(sender);

            // Loop over priority tasks
            for(Task updatedPriorityTask: updatedPriorityTasks) {
                // Check if the current priority tasks does not contain the updated priority task and the agent has a color and the color of the agent equals the color of the packet of the updated priority task
                if (!currentPriorityTasks.contains(updatedPriorityTask) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() == updatedPriorityTask.getPacket().getRgbColor()) {
                    // Add the updated priority task to the current priority task
                    currentPriorityTasks.add(updatedPriorityTask);
                }
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PRIORITY_TASKS, currentPriorityTasks));
    }

    ///////////
    // UTILS //
    ///////////
    
    /**
     * Has the agent reached the position?
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to reach
     * @return True is the agent is next to the position, otherwise false
     */
    public static boolean hasReachedPosition(AgentState agentState, Coordinate coordinate) {
        // Get the position of the agent and the position
        int agentX = agentState.getX();
        int agentY = agentState.getY();
        int coordinateX = coordinate.getX();
        int coordinateY = coordinate.getY();

        // Calculate the difference between the positions
        int dX = Math.abs(agentX - coordinateX);
        int dY = Math.abs(agentY - coordinateY);

        // Return true if the distance is less than 1 for both axes, otherwise return false
        return (dX <= 1) && (dY <= 1);
    }
    
    
    /**
     * Does the graph contains a node with a given coordinate?
     * 
     * @param agentState The current state of the agent
     * @param coordinate The coordinate of the position to check
     * @return True is the position is in the graph, otherwise false
     */
    public static boolean isCoordinateInGraph(AgentState agentState, Coordinate coordinate) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Check if the graph is null and return false turn if so
        if(graph == null) {
            return false;
        }

        // Loop over the graph nodes
        for(Node node: graph.getMap().keySet()) {
            // Get the coordinate of the node
            Coordinate nodeCoordinate = node.getCoordinate();

            // Check if the coordinate of the node equals the coordinate of the position
            if(nodeCoordinate.equals(coordinate)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculate the Euclidean distance between two coordinates
     *
     * @param coordinate1 The first coordinate
     * @param coordinate2 The second coordinate
     * @return The euclidean distance between the two coordinates
     */
    public static double calculateEuclideanDistance(Coordinate coordinate1, Coordinate coordinate2) {
        // Get the positions of both coordinates
        int coordinate1X = coordinate1.getX();
        int coordinate1Y = coordinate1.getY();
        int coordinate2X = coordinate2.getX();
        int coordinate2Y = coordinate2.getY();

        // Calculate and return the distance
        return Math.sqrt(((coordinate2Y - coordinate1Y) * (coordinate2Y - coordinate1Y)) + ((coordinate2X - coordinate1X) * (coordinate2X - coordinate1X)));
    }

    /**
     * Has the agent enough battery to complete a task?
     *
     * @param agentState The current state of the agent
     * @param packet The packet the agent has to deliver
     * @param destination The destination to which the agent has to deliver the packet
     * @return True if enough energy to perform the task, false otherwise.
     */
    public static boolean hasEnoughBatteryToCompleteTask(AgentState agentState, Packet packet, Destination destination) {
        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Get the position of the packet
        int packetX = packet.getCoordinate().getX();
        int packetY = packet.getCoordinate().getY();

        // Get the number of cells between the agent and the packet
        int numberOfCellsToPacket = Perception.distance(agentX, agentY, packetX, packetY);

        // Calculate the energy required to go to the packet
        double requiredEnergyToPacket = numberOfCellsToPacket * GeneralUtils.COST_WALK_WITHOUT_PACKET;

        // Get the position of the destination
        int destinationX = destination.getCoordinate().getX();
        int destinationY = destination.getCoordinate().getY();

        // Get the number of cells between the packet and the destination
        int numberOfCellsToDestination = Perception.distance(packetX, packetY, destinationX, destinationY);

        // Calculate the energy required to go to the destination
        double requiredEnergyToDestination = numberOfCellsToDestination * GeneralUtils.COST_WALK_WITH_PACKET;

        // Calculate total required energy
        // Hereby, a margin of 150 is added.
        double totalRequiredEnergy = requiredEnergyToPacket + requiredEnergyToDestination + 150;

        // Return true if the battery state of the agent is higher than the total required energy, otherwise return false
        return agentState.getBatteryState() > totalRequiredEnergy;
    }

    /**
     * Extract the target if one exists in the cell perception
     *
     * @param cellPerception The perception of the cell
     * @return A target or empty if no one exists
     */
    public static Optional<Target> extractTarget(CellPerception cellPerception) {
        // Get the coordinate of the target
        Coordinate targetCoordinate = new Coordinate(cellPerception.getX(), cellPerception.getY());

        // Check if the cell contains a packet
        if (cellPerception.containsPacket()) {
            return Optional.of(new Packet(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(PacketRep.class)).getColor().getRGB()));
        }
        // Check if the cell contains a destination
        if (cellPerception.containsAnyDestination()) {
            return Optional.of(new Destination(targetCoordinate, Objects.requireNonNull(cellPerception.getRepOfType(DestinationRep.class)).getColor().getRGB()));
        }
        // Check if the cell contains a charging station
        if (cellPerception.containsEnergyStation()) {
            return Optional.of(new ChargingStation(targetCoordinate));
        }

        return Optional.empty();
    }

    /**
     * Can a destination be reached
     * 
     * @param agentState The agent state
     * @param task The task
     * @return True if destination can be reached
     */
    public static boolean canDestinationBeReached(AgentState agentState, Task task) {
        // Get the destinations
        ArrayList<Destination> destinations = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class).getTargets(Destination.class);

        // Loop over the destinations
        for (Destination candidateDestination : destinations) {
            // Get the color of the candidate destination
            Color candidateDestinationColor = candidateDestination.getColor();

            // Check if the candidate destination color does not equal the packet color
            if (candidateDestinationColor.getRGB() != task.getPacket().getRgbColor()) {
                continue;
            }

            // Check if the agent has not enough battery to complete the task
            if (!GeneralUtils.hasEnoughBatteryToCompleteTask(agentState, task.getPacket(), candidateDestination)) {
                continue;
            }

            // Get the path by means of A*
            ArrayList<Node> destinationPath = GraphUtils.performAStarSearch(agentState, candidateDestination.getCoordinate(), false);

            // Check if the path is not null
            if (destinationPath != null) {
                // Get the second last coordinate in the path
                Coordinate destinationCoordinate = destinationPath.get(destinationPath.size()-1).getCoordinate();

                // Create a destination
                Destination destination = new Destination(destinationCoordinate, task.getPacket().getRgbColor());

                // Set the destination to the task
                task.setDestination(destination);

                // Update the memory
                MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.TASK, task));

                return true;
            }
        }

        return false;
    }

    /**
     * Are the task conditions satisfied
     * 
     * @param agentState The current state of the agent
     * @param task The task
     * @return True is the conditions are satisfied, otherwise false
     */
    public static boolean areConditionsSatisfied(AgentState agentState, Task task) {
        // Get the graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        // Loop over the conditions of the task
        for(Packet packet : task.getConditions()) {
            // Check if the node still contains a packet
            if (graph.getNode(packet.getCoordinate()).get().containsPacket()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Create priority tasks
     * 
     * @param agentState The current state of the agent
     * @param path The path
     */
    public static void createPriorityTasks(AgentState agentState, ArrayList<Node> pathPackets) {
        // Create the task conditions
        ArrayList<Packet> taskConditions = new ArrayList<>();

        // Get the priority tasks
        ArrayList<Task> priorityTasks = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS, Task.class);
        ArrayList<Task> priorityTasksSend = MemoryUtils.getListFromMemory(agentState, MemoryKeys.PRIORITY_TASKS_SEND, Task.class);

        // Loop over the packet nodes in the path
        for (Node packetNode : pathPackets.stream().filter(n -> n.containsPacket()).collect(Collectors.toList())) {
            Packet packet = (Packet) packetNode.getTarget().get();
            Task task = new Task(packet, null);
            task.setConditions(taskConditions);
            taskConditions.add(packet);

            // Check if agent can not handle the task
            if (!priorityTasksSend.contains(task) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() != packet.getRgbColor()){
                priorityTasksSend.add(task);
            }
            else if (!priorityTasks.contains(task) && agentState.getColor().isPresent() && agentState.getColor().get().getRGB() == packet.getRgbColor()){
                priorityTasks.add(task);
            }
        }

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(MemoryKeys.PRIORITY_TASKS, priorityTasks, MemoryKeys.PRIORITY_TASKS_SEND, priorityTasksSend));
    }
}