package util.assignments.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import agent.AgentState;
import environment.Coordinate;
import util.assignments.jackson.JacksonUtils;

/**
 * A class that implements functions regarding the memory of the agent
 */
public class MemoryUtils {

    /////////////
    // GETTERS //
    /////////////

    /**
     * A function to get an object from the memory
     * 
     * @param <T> The type of the object 
     * @param agentState The current state of the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the object
     * @return The object or null if no object was found
     */
    public static <T> T getObjectFromMemory(AgentState agentState, String memoryKey, Class<T> objectClass) {
        // Get the memory
        Set<String> memory = agentState.getMemoryFragmentKeys();

        // Check if the memory contains the memory fragment
        if(memory.contains(memoryKey)) {
            // Get the memory fragment
            String objectString = agentState.getMemoryFragment(memoryKey);

            // Transform the string to a JSON object and return
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            try {
                return objectMapper.readValue(objectString, objectClass);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        else return null;
    }

    /**
     * A function to get a list from the memory and to create one if no exists yet
     * 
     * @param <T> The type of the objects contained in the list
     * @param agentState The current state of the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the objects contained in the list
     * @return The list of objects
     */
    public static <T> ArrayList<T> getListFromMemory(AgentState agentState, String memoryKey, Class<T> objectClass) {
        // Get the memory
        Set<String> memory = agentState.getMemoryFragmentKeys();

        // Check if the memory contains the memory fragment
        if(memory.contains(memoryKey)) {
            // Get the memory fragment
            String listString = agentState.getMemoryFragment(memoryKey);

            // Transform the string to a JSON object and return
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            try {
                return objectMapper.readValue(listString, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, objectClass));
            } catch (IOException e) {
                e.printStackTrace();
                return new ArrayList<>();
            }
        }
        else {
            // Initialize a new list
            ArrayList<T> list = new ArrayList<>();

            // Update the memory
            updateMemory(agentState, Map.of(memoryKey, list));

            // Return the initialized list
            return list;
        }
    }

    ////////////
    // UPDATE //
    ////////////

    /**
     * A function to update the memory. 
     * It deletes the values of the memory fragments of the given keys and replaces them with the values of {@code updates}.
     * 
     * @param agentState The current state of the agent
     * @param updates A list of updates that should be done
     */
    public static void updateMemory(AgentState agentState, Map<String, Object> updates) {
        // Get the memory
        Set<String> memory = agentState.getMemoryFragmentKeys();

        for(String memoryKey: updates.keySet()) {
            // Remove the memory fragment from the memory if it exists
            if(memory.contains(memoryKey)) 
                agentState.removeMemoryFragment(memoryKey);

            // Transform the object to a JSON string
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            String objectString = "";
            try {
                objectString = objectMapper.writeValueAsString(updates.get(memoryKey));
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Add the update to the memory
            agentState.addMemoryFragment(memoryKey, objectString);
        }
    }
}
