package util.assignments.memory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;

import agent.AgentState;
import util.assignments.jackson.JacksonUtils;

/**
 * A class that implements functions regarding the memory of the agent
 */
public class MemoryUtils {

    /////////////
    // METHODS //
    /////////////

    /**
     * Get an object from memory
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

        // Check if the memory contains the memory key
        if(memory.contains(memoryKey)) {
            // Get the memory fragment
            String objectString = agentState.getMemoryFragment(memoryKey);

            // Transform the JSON string to an object
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            try {
                return objectMapper.readValue(objectString, objectClass);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        return null;
    }

    /**
     * Get a list from memory
     * Create one if it does not exist
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

        // Check if the memory contains the memory key
        if(memory.contains(memoryKey)) {
            // Get the memory fragment
            String listString = agentState.getMemoryFragment(memoryKey);

            // Transform the JSON string to a list
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            try {
                return objectMapper.readValue(listString, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, objectClass));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Initialize a new list
        ArrayList<T> list = new ArrayList<>();

        // Update the memory
        MemoryUtils.updateMemory(agentState, Map.of(memoryKey, list));

        // Return the initialized list
        return list;
    }

    /**
     * Update the memory
     * 
     * @param agentState The current state of the agent
     * @param updates A list of updates
     */
    public static void updateMemory(AgentState agentState, Map<String, Object> updates) {
        // Get the memory
        Set<String> memory = agentState.getMemoryFragmentKeys();

        // Loop over the updates
        for(String memoryKey: updates.keySet()) {
            // Check if the memory contains the memory key
            if(memory.contains(memoryKey))
                // Remove the fragment from the memory
                agentState.removeMemoryFragment(memoryKey);

            try {
                // Transform the object to a JSON string
                ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
                String objectString = objectMapper.writeValueAsString(updates.get(memoryKey));

                // Add the fragment to the memory
                agentState.addMemoryFragment(memoryKey, objectString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}