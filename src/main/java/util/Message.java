package util;

import com.google.gson.Gson;

import java.util.HashMap;

public class Message {
    String message;
    String type;

    public Message(String msg, String kind) {
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
