package util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

import java.util.HashMap;

public class Message {
    String message;
    String type;

    @JsonCreator
    public Message(@JsonProperty("msg") String msg, @JsonProperty("type") String kind) {
        message = msg;
        type = kind;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String msgToSendFormat() {

        Gson gson = new Gson();
        HashMap<String, String> msg = new HashMap<>();
        msg.put(type, message);

        return gson.toJson(msg);
    }
}
