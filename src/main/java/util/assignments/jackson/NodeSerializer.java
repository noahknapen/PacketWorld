package util.assignments.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;

import util.assignments.graph.Node;

/**
 * A class that implements a function to serialize a Node class
 */
public class NodeSerializer extends JsonSerializer<Node> {

    @Override
    public void serialize(Node node, JsonGenerator jsonGenerator, SerializerProvider serializers) throws IOException, JsonProcessingException {
        ObjectMapper objectMapper = JacksonUtils.buildObjectMapper();
        String nodeString = objectMapper.writeValueAsString(node);
        jsonGenerator.writeFieldName(nodeString);
    }
}