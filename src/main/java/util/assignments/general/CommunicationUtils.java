package util.assignments.general;

import java.io.IOException;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import agent.AgentCommunication;
import agent.AgentState;
import environment.Mail;
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
     * A function to get an object from the received mails
     * 
     * @param <T> The type of the object 
     * @param agentCommunication Perform communication with the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the object
     * @return The object or null if no object was found
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static <T> T getObjectFromMails(AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) throws JsonParseException, JsonMappingException, IOException {
        // Get the received mails
        ArrayList<Mail> mails = new ArrayList<>(agentCommunication.getMessages());
    
        // Loop over all the received mails
        ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
        for(int i = 0; i < mails.size(); i++) {
            // Get the mail
            Mail mail = mails.get(i);

            // Get the message
            String messageString = mail.getMessage();
            Message message = objectMapper.readValue(messageString, Message.class);
            
            // Check if type corresponds
            if(message.getType().equals(memoryKey)) {
                // Remove the message from the mails
                agentCommunication.removeMessage(i);

                // Transform the message and return
                return objectMapper.readValue(message.getMessage(), objectClass);
            }
        }

        return null;
    }

    /**
     * A function to get a list from the received mails
     * 
     * @param <T> The type of the objects contained in the list
     * @param agentCommunication Perform communication with the agent
     * @param memoryKey The memory key
     * @param objectClass The class of the objects contained in the list
     * @return The list of objects or null if no list was found
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonParseException
     */
    public static <T> ArrayList<T> getListFromMails(AgentState agentState, AgentCommunication agentCommunication, String memoryKey, Class<T> objectClass) throws JsonParseException, JsonMappingException, IOException {
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
    }


    ///////////////
    // BROADCAST //
    ///////////////

    public static void broadcastMemoryFragment(AgentState agentState, AgentCommunication agentCommunication, String memoryKey) throws JsonProcessingException {
        // Get the memory fragment in JSON string
        String memoryFragmentString = agentState.getMemoryFragment(memoryKey);

        // Check if the memory fragment is null and return if so
        if(memoryFragmentString == null) return;

        

        // Create a message string
        ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
        Message message = new Message(memoryFragmentString, memoryKey);
        String messageString = objectMapper.writeValueAsString(message);       

        // Broadcast the message
        agentCommunication.broadcastMessage(messageString);
    }
}
