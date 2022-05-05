package util.assignments.task;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

/**
 * A class representing the task the agent is performing
 */
public class Task {
    
    private TaskType type;
    private Packet packet;
    private Destination destination;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    @JsonCreator
    public Task(@JsonProperty("type") TaskType type, @JsonProperty("packet") Packet packet, @JsonProperty("destination") Destination destination) {
        this.setType(type);
        this.setPacket(packet);
        this.setDestination(destination);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////
    
    public TaskType getType() {
        return type;
    }

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

    public void setType(TaskType type) {
        this.type = type;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s", type, packet, destination);
    }
}
