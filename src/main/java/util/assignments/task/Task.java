package util.assignments.task;

import com.google.common.base.Optional;

import agent.behavior.assignment_1_A.utils.Destination;
import util.assignments.targets.Packet;

/**
 * A class representing the task the agent is performing
 */
public class Task {
    
    private TaskType type;
    private Optional<Packet> packet;
    private Optional<Destination> destination;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public Task(TaskType type) {
        this.setType(type);
        this.setPacket(Optional.absent());
        this.setDestination(Optional.absent());
    }

    public Task(TaskType type, Optional<Packet> packet, Optional<Destination> destination) {
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

    public Optional<Packet> getPacket() {
        return packet;
    }

    public Optional<Destination> getDestination() {
        return destination;
    }

    public void setPacket(Optional<Packet> packet) {
        this.packet = packet;
    }

    public void setType(TaskType type) {
        this.type = type;
    }

    public void setDestination(Optional<Destination> destination) {
        this.destination = destination;
    }    
}
