package util.assignments.jackson;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import util.assignments.graph.Node;

/**
 * A class that implements a function to serialize a the Node class
 */
public class NodeSerializer extends JsonSerializer<Node> {

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void serialize(Node value, 
      JsonGenerator gen,
      SerializerProvider serializers) 
      throws IOException, JsonProcessingException {
 
        mapper.registerModules(new Jdk8Module());
        String json = mapper.writeValueAsString(value);
        gen.writeFieldName(json);
    }
}
