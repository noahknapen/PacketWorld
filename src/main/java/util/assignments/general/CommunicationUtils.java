package util.assignments.general;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;

import agent.AgentCommunication;
import agent.AgentState;
import environment.CellPerception;
import environment.Mail;
import environment.Perception;
import environment.world.agent.AgentRep;
import util.Message;
import util.assignments.jackson.JacksonUtils;

/**
 * A class that implements functions regarding the communication of the agent
 */
public class CommunicationUtils {

    /////////////
    // RECEIVE //
    /////////////

    /**
     * Get a hashmap of objects of a specific type from the received mails
     * 
     * @param <T> The type of the object 
     * @param agentCommunication The communication interface of the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the object
     * @return The hashmap containing all corresponding objects linked with the sender
     */
    public static <T> HashMap<String, T> getObjectsFromMails(AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) {
        try {
            // Check if there are not messages and return an empty hashmap if so
            if (agentCommunication.getNbMessages() == 0) return new HashMap<>();

            // Get the received mails
            ArrayList<Mail> receivedMails = new ArrayList<>(agentCommunication.getMessages());

            // Create a result hashmap
            HashMap<String, T> result = new HashMap<>();

            // Loop over all the received mails
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            int messagesNotCorresponding = 0;
            for(int i = 0; i < receivedMails.size(); i++) {
                // Get the received mail
                Mail receivedMail = receivedMails.get(i);

                // Get the message and the sender
                String messageString = receivedMail.getMessage();
                Message message = objectMapper.readValue(messageString, Message.class);
                String sender = receivedMail.getFrom();

                // Check if the type of the message is not equal to the memory key and continue with the next mail if so
                if(!message.getType().equals(memoryKey)) {
                    messagesNotCorresponding++;
                    continue;
                }

                // Remove the message from the mails
                agentCommunication.removeMessage(messagesNotCorresponding);

                // Get the object
                String objectString = message.getMessage();
                T object = objectMapper.readValue(objectString, objectClass);

                // Put the object in the map
                result.put(sender, object);
            } 
            return result;

        } catch(IOException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * Get a hashmap of lists of objects of a specific type from the received mails
     * 
     * @param <T> The type of the object 
     * @param agentCommunication The communication interface of the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the object
     * @return The hashmap containing all corresponding lists of objects linked with the sender
     */
    public static <T> HashMap<String, ArrayList<T>> getObjectListsFromMails(AgentState agentState, AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) {
        try {
            // Check if there are not messages and return an empty hashmap if so
            if (agentCommunication.getNbMessages() == 0) return new HashMap<>();

            // Get the received mails
            ArrayList<Mail> receivedMails = new ArrayList<>(agentCommunication.getMessages());

            // Create a result hashmap
            HashMap<String, ArrayList<T>> result = new HashMap<>();

            // Loop over all the received mails
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            for(int i = 0; i < receivedMails.size(); i++) {
                // Get the received mail
                Mail receivedMail = receivedMails.get(i);

                // Get the message and the sender
                String messageString = receivedMail.getMessage();
                Message message = objectMapper.readValue(messageString, Message.class);
                String sender = receivedMail.getFrom();

                // Check if the type of the message is not equal to the memory key and continue with the next mail if so
                if(!message.getType().equals(memoryKey)) continue;

                // Remove the message from the mails
                agentCommunication.removeMessage(i);

                // Get the object
                String objectString = message.getMessage();
                ArrayList<T> objectList = objectMapper.readValue(objectString, objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, objectClass));

                // Put the object in the map
                result.put(sender, objectList);
            }
            
            return result;
        } catch(IOException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    //////////
    // SEND //
    //////////

    /**
     * Send a memory fragment to another agent
     * 
     * @param agentState The current state of the agent
     * @param agentCommunication The communication interface of the agent
     * @param memoryKey The memory key
     */
    public static void sendMemoryFragment(AgentState agentState, AgentCommunication agentCommunication, String memoryKey) {
        // Check if the memory key does not exist and return if so
        if(!agentState.getMemoryFragmentKeys().contains(memoryKey)) {
            return;
        }

        // Get the memory fragment string
        String memoryFragmentString = agentState.getMemoryFragment(memoryKey);

        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) {
                    continue;
                }

                // Get the agent representation of the cell
                Optional<AgentRep> agentRepresentation = cellPerception.getAgentRepresentation();

                // Check if the agent representation is empty and continue with the next cell if so
                // It checks if there is no agent on the cell.
                if (agentRepresentation.isEmpty()) {
                    continue;
                }

                // Check if the position of the agent corresponds to the agent's own position and continue with the next cell if so
                if (agentRepresentation.get().getX() == agentX && agentRepresentation.get().getY() == agentY) {
                    continue;
                }

                // Create a message string
                String messageString = CommunicationUtils.makeMessageString(memoryKey, memoryFragmentString);

                // Communicate the message to the agent
                agentCommunication.sendMessage(agentRepresentation.get(), messageString);
            }
        } 
    }


    ///////////////
    // BROADCAST //
    ///////////////

    /**
     * A function that is used to broadcast memory fragments. It is only used for broadcasting charging stations.
     *
     * @param agentState: State of the agent
     * @param agentCommunication The interface for communication
     * @param memoryKey: The key to find the memory fragment
     */
    public static void broadcastMemoryFragment(AgentState agentState, AgentCommunication agentCommunication, String memoryKey) {
        // Check if the memory key does not exist and return if so
        if(!agentState.getMemoryFragmentKeys().contains(memoryKey)) {
            return;
        }

        // Get the memory fragment string
        String memoryFragmentString = agentState.getMemoryFragment(memoryKey);

        // Create a message string
        String messageString = makeMessageString(memoryKey, memoryFragmentString);

        // Broadcast the message 
        agentCommunication.broadcastMessage(messageString);
    }

    ///////////
    // UTILS //
    ///////////

    /**
     * A function that makes a message string.
     *
     * @param memoryKey The memory key
     * @param memoryFragmentString The memory fragment as a string
     * @return The string that can be sent
     */
    private static String makeMessageString(String memoryKey, String memoryFragmentString) {
        try {
            // Create an objectmapper
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();

            // Create a message
            Message message = new Message(memoryFragmentString, memoryKey);

            // Create a message string
            String messageString = objectMapper.writeValueAsString(message);

            return messageString;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * A help function to send an emergency message to the agent that is using the charging station.
     *
     * @param agentState: The state of the agent
     * @param agentCommunication: The interface for communication
     * @param msg: The message we want to send
     * @param type: The type of message
     *
     * @return true if the message was sent to somebody, false otherwise
     */
    public static boolean sendEmergencyMessage(AgentState agentState, AgentCommunication agentCommunication, String msg, String type) {
        Perception agentPerception = agentState.getPerception();

        // Create a message string
        String messageString = makeMessageString(type, msg);
        boolean sent = false;

        for (int x = 0; x <= agentPerception.getWidth(); x++) {
            for (int y = 0; y <= agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x, y);
                CellPerception stationCellPerception = agentPerception.getCellAt(x, y + 1);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) continue;

                // Check if the cell beneath the cellPerception is null
                if (stationCellPerception == null) continue;

                // Get the agent representation of the cell
                Optional<AgentRep> agentRep = cellPerception.getAgentRepresentation();

                // Check if there is no agent on the cell and continue with the next cell if so
                if (agentRep.isEmpty()) continue;

                // Only send message to the agent on the charging station
                if (!stationCellPerception.containsEnergyStation()) continue;

                // Check if the position of the agent corresponds to the agent's own position and continue with the next cell if so
                if (agentRep.get().getX() == agentState.getX() && agentRep.get().getY() == agentState.getY()) continue;

                // Communicate the message to the agent
                agentCommunication.sendMessage(agentRep.get(), messageString);

                // Update sent variable
                sent = true;
            }
        }

        return sent;
    }
}
