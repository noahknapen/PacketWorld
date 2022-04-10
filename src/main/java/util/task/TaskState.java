package util.task;

/**
 *  A class representing a task state
 *  - RANDOM: No actual task
 *  - TO_PACKET: The goal is pick up a pack
 *  - TO_DESTINATION: The goal is to put the packetat the destination
 */
public enum TaskState {
    RANDOM,
    TO_PACKET,
    TO_DESTINATION
}