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
import util.assignments.targets.Packet;

/**
 * A class that implements functions regarding the communication of the agent
 */
public class CommunicationUtils {

    /////////////
    // RECEIVE //
    /////////////

    /**
     * A function to get an object from the received mails
     * 
     * @param <T> The type of the object 
     * @param agentCommunication The interface for communication
     * @param memoryKey The memory key
     * @param objectClass The class of the object
     * @return The object or null if no object was found
     */
    public static <T> HashMap<String, T > getObjectFromMails(AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) {
        try {
            // Get the received mails
            ArrayList<Mail> mails = new ArrayList<>(agentCommunication.getMessages());

            HashMap<String, T> result = new HashMap<>();

            // Loop over all the received mails
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            for(int i = 0; i < mails.size(); i++) {
                // Get the mail
                Mail mail = mails.get(i);

                // Get the message
                String messageString = mail.getMessage();
                String sender = mail.getFrom();
                Message message = objectMapper.readValue(messageString, Message.class);

                // Guard clause to ensure the type corresponds
                if(!message.getType().equals(memoryKey)) continue;

                // Remove the message from the mails
                agentCommunication.removeMessage(i);

                // Transform the message and return
                result.put(sender, objectMapper.readValue(message.getMessage(), objectClass));

                return result;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T> HashMap<String, ArrayList<T>> getSenderListFromMails(AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) {
        try {
            // Get the received mails
            ArrayList<Mail> mails = new ArrayList<>(agentCommunication.getMessages());

            HashMap<String, ArrayList<T>> result = new HashMap<>();

            // Loop over all the received mails
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            for(int i = 0; i < mails.size(); i++) {
                // Get the mail
                Mail mail = mails.get(i);

                // Get the message
                String messageString = mail.getMessage();
                String sender = mail.getFrom();
                Message message = objectMapper.readValue(messageString, Message.class);

                // Guard clause to ensure the type corresponds
                if(!message.getType().equals(memoryKey)) continue;

                // Remove the message from the mails
                agentCommunication.removeMessage(i);

                // Transform the message and return
                result.put(sender, objectMapper.readValue(message.getMessage(), objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, objectClass)));

                return result;
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    /**
     * A function to get a list from the received mails
     * 
     * @param <T> The type of the objects contained in the list
     * @param agentCommunication The interface for communication
     * @param memoryKey The memory key
     * @param objectClass The class of the objects contained in the list
     * @return The list of objects or null if no list was found
     */
    public static <T> ArrayList<T> getListFromMails(AgentState agentState, AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) {
        try {
            // If no messages, return empty list
            if (agentCommunication.getNbMessages() == 0) return new ArrayList<>();

            // Get the received mails
            ArrayList<Mail> mails = new ArrayList<>(agentCommunication.getMessages());

            // Loop over all the received mails
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            ArrayList<T> result = new ArrayList<>();

            for(int i = 0; i < mails.size(); i++) {
                // Get the mail
                Mail mail = mails.get(i);

                // Check if the mails is one of its own and continue with the next mail is so
                if(agentState.getName().equals(mail.getFrom())) continue;

                // Get the message
                String messageString = mail.getMessage();
                Message message = objectMapper.readValue(messageString, Message.class);

                // Check if type corresponds
                if(message.getType().equals(memoryKey)) {
                    // Transform the message and return
                    result = objectMapper.readValue(message.getMessage(), objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, objectClass));

                    // Remove the message from the mails
                    agentCommunication.removeMessage(i);

                    break;
                }
            }
            return result;
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    //////////
    // SEND //
    //////////

    public static void sendMemoryFragment(AgentState agentState, AgentCommunication agentCommunication, String memoryKey) {

        // Check if memoryKey exists
        if(!agentState.getMemoryFragmentKeys().contains(memoryKey)) return;

        // Get the perception of the agent
        Perception agentPerception = agentState.getPerception();

        // Get the position of the agent
        int agentX = agentState.getX();
        int agentY = agentState.getY();

        // Get the memory fragment in JSON string
        String memoryFragmentString = agentState.getMemoryFragment(memoryKey);

        // Loop over the whole perception
        for (int x = 0; x < agentPerception.getWidth(); x++) {
            for (int y = 0; y < agentPerception.getHeight(); y++) {
                // Get the perception of the cell
                CellPerception cellPerception = agentPerception.getCellAt(x, y);

                // Check if the cell is null and continue with the next cell if so
                if (cellPerception == null) continue;

                // Get the agent representation of the cell
                Optional<AgentRep> agentRep = cellPerception.getAgentRepresentation();

                // Check if there is no agent on the cell and continue with the next cell if so
                if (agentRep.isEmpty()) continue;

                // Check if the position of the agent corresponds to the agent's own position and continue with the next cell if so
                if (agentRep.get().getX() == agentX && agentRep.get().getY() == agentY) continue;

                // Create a message string
                String messageString = makeMessageString(memoryFragmentString, memoryKey);

                // Communicate the message to the agent
                agentCommunication.sendMessage(agentRep.get(), messageString);
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
        // Get the memory fragment in JSON string
        String memoryFragmentString = agentState.getMemoryFragment(memoryKey);

        // Check if the memory fragment is null and return if so
        if (memoryFragmentString == null) return;

        // Create a message string
        String messageString = makeMessageString(memoryFragmentString, memoryKey);

        // Broadcast the message
        agentCommunication.broadcastMessage(messageString);
    }

    ///////////
    // UTILS //
    ///////////

    /**
     * A function that makes a message string.
     *
     * @param memoryFragmentString The memory fragment as a string
     * @param memoryKey The memory key
     * @return The string that can be sent
     */
    private static String makeMessageString(String memoryFragmentString, String memoryKey) {
        try {
            // Define a message, transform it to a JSON and return it
            ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
            Message message = new Message(memoryFragmentString, memoryKey);
            return objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
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
        String messageString = makeMessageString(msg, type);
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
