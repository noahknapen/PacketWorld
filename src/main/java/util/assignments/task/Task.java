package util.assignments.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import util.assignments.graph.Node;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

import java.util.ArrayList;

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

    private ArrayList<Packet> taskConditions = new ArrayList<>();

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    @JsonCreator
    public Task(@JsonProperty("packet") Packet packet, @JsonProperty("destination") Destination destination) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setIsMoveTask(false);
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

    public boolean isMoveTask() {
        return isMoveTask;
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

    public ArrayList<Packet> getTaskConditions() {
        return taskConditions;
    }

    public void setTaskConditions(ArrayList<Packet> taskConditions) {
        this.taskConditions = taskConditions;
    }
}
