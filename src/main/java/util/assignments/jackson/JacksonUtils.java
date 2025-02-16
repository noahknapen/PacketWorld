package util.assignments.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

/**
 * A class that implements functions regarding the Jackson library
 */
public class JacksonUtils {

    /**
     * Build a parametrized object mapper
     * 
     * @return The parametrized object mapper
     */
    public static ObjectMapper buildObjectMapper() {
        // Initialize an object mapper
        ObjectMapper objectMapper = new ObjectMapper();

        // Parametrize the object mapper
        objectMapper.registerModule(new Jdk8Module());

        return objectMapper;   
    }
}