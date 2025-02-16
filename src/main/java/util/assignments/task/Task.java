package util.assignments.task;

import java.util.ArrayList;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import util.assignments.targets.Destination;
import util.assignments.targets.Packet;

/**
 * A class representing a task the agent performs
 */
public class Task {
    
    // A data member holding the packet linked to the task
    private Packet packet;
    // A data member holding the destination linked to the task
    private Destination destination;
    // A data member holding if a task is handled or not
    private boolean handled;
    // A data member holding the conditions for the task to be handled
    private ArrayList<Packet> conditions;

    //////////////////
    // CONSTRUCTORS //
    //////////////////
    
    public Task(Packet packet, Destination destination) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setHandled(false);
        this.setConditions(new ArrayList<>());
    }

    @JsonCreator
    public Task(@JsonProperty("packet") Packet packet, @JsonProperty("destination") Destination destination, @JsonProperty("handled") boolean handled, @JsonProperty("conditions") ArrayList<Packet> conditions) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setHandled(handled);
        this.setConditions(conditions);
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

    public boolean isHandled() {
        return handled;
    }

    public ArrayList<Packet> getConditions() {
        return conditions;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public void setConditions(ArrayList<Packet> conditions) {
        if (this.conditions != null)
        {
            this.conditions.clear();
            this.conditions.addAll(conditions);
        }
        else
        {
            this.conditions = new ArrayList<>();
        }
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s %s", packet, destination, handled, conditions);
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Task task) {
            result = packet.equals(task.getPacket()) &&
                    ((destination == null && task.getDestination() == null) || destination.equals(task.getDestination()));
        }

        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(packet, destination);
    }
}