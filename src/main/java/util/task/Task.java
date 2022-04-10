package util.task;

import com.google.gson.Gson;

import util.targets.Destination;
import util.targets.Packet;

/**
 *  A class representing a task
 */
public class Task {

    private Packet packet;
    private Destination destination;
    private TaskState state;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public Task(Packet packet, Destination destination, TaskState state) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setState(state);
    }

    //////////////////////
    // GETTERS & SETTERS//
    //////////////////////
    
    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet= packet;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public TaskState getState() {
        return state;
    }

    public void setState(TaskState state) {
        this.state = state;
    }

    //////////
    // JSON //
    //////////

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Task fromJson(String taskString) {
        Gson gson = new Gson();
        return gson.fromJson(taskString, Task.class);
    }
}
