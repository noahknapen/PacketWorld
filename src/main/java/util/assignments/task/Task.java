package util.assignments.task;

import agent.AgentState;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import util.assignments.graph.Graph;
import util.assignments.graph.Node;
import util.assignments.memory.MemoryKeys;
import util.assignments.memory.MemoryUtils;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.targets.Target;

import java.util.ArrayList;
import java.util.Optional;

/**
 * A class representing the task the agent is performing
 */
public class Task {
    
    // A data member holding the packet linked to the task
    private Packet packet;
    // A data member holding the destination linked to the task
    private Destination destination;
    // A data member that tells if task is a move packet task or not
    @JsonProperty("isMoveTask")
    private boolean isMoveTask;

    private ArrayList<Packet> taskConditions;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    @JsonCreator
    public Task(@JsonProperty("packet") Packet packet, @JsonProperty("destination") Destination destination) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setIsMoveTask(false);
        taskConditions = new ArrayList<>();
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public Packet getPacket() {
        return packet;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void setIsMoveTask(boolean moveTask) {
        this.isMoveTask = moveTask;
    }

    ///////////////
    // OVERRIDES //

    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s", packet, destination, isMoveTask);
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Task otherTask) {
            result = this.getPacket().equals(otherTask.getPacket()) && this.getDestination().equals(otherTask.getDestination());
        }

        return result;
    }

    public ArrayList<Packet> getTaskConditions() {
        return taskConditions;
    }

    public void setTaskConditions(ArrayList<Packet> taskConditions) {
        this.taskConditions.clear();
        this.taskConditions.addAll(taskConditions);
    }

    public boolean conditionsSatisfied(AgentState agentState) {

        // Get graph
        Graph graph = MemoryUtils.getObjectFromMemory(agentState, MemoryKeys.GRAPH, Graph.class);

        for (Packet packet : taskConditions) {
            if (graph.getNode(packet.getCoordinate()).isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
