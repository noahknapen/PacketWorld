package util.tasks;

/**
 *  A class representing a task
 */
public class Task {

    private Packet packet;
    private Destination destination;
    private TaskState taskState;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public Task(Packet packet, Destination destination, TaskState taskState) {
        this.setPacket(packet);
        this.setDestination(destination);
        this.setTaskState(taskState);
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

    public TaskState getTaskState() {
        return taskState;
    }

    public void setTaskState(TaskState taskState) {
        this.taskState = taskState;
    }
}
