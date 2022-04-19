package util.assignments.memory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import agent.AgentState;

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
            GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization();
            Gson gson = gsonBuilder.create();
            return gson.fromJson(objectString, objectClass);
        }
        else return null;
    }

    /**
     * A function to get a list from the memory
     * 
     * @param <T> The type of the objects contained in the list
     * @param agentState The current state of the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the objects contained in the list
     * @return The list of objects or null if no list was found
     */
    public static <T> ArrayList<T> getListFromMemory(AgentState agentState, String memoryKey, Class<T> objectClass) {
        // Get the memory
        Set<String> memory = agentState.getMemoryFragmentKeys();

        // Check if the memory contains the memory fragment
        if(memory.contains(memoryKey)) {
            // Get the memory fragment
            String objectString = agentState.getMemoryFragment(memoryKey);

            // Transform the string to a JSON object and return
            GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization();
            Gson gson = gsonBuilder.create();
            return gson.fromJson(objectString, new TypeToken<ArrayList<T>>(){}.getType());
        }
        else return null;
    }

    ////////////
    // UPDATE //
    ////////////

    /**
     * A function to update the memory
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
            GsonBuilder gsonBuilder = new GsonBuilder().enableComplexMapKeySerialization();
            Gson gson = gsonBuilder.create();
            String objectString = gson.toJson(updates.get(memoryKey));

            // Add the update to the memory
            agentState.addMemoryFragment(memoryKey, objectString);
        }
    }
}
