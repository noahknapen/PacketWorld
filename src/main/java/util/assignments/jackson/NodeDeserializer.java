package util.assignments.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import util.assignments.graph.Node;

/**
 * A class that implements a function to deserialize a Node class
 */
public class NodeDeserializer extends KeyDeserializer {

    @Override
    public Node deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException { 
      ObjectMapper mapper = JacksonUtils.buildObjectMapper();
      Node node = mapper.readValue(key, Node.class);
      return node;
    }
}