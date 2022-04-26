package util.assignments.gson;

import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import util.assignments.targets.ChargingStation;
import util.assignments.targets.Destination;
import util.assignments.targets.Packet;
import util.assignments.targets.Target;

/**
 * A class that implements functions regarding the Gson library
 */
public class GsonUtils {

    /**
     * A function to build a parametrized Gson object
     * 
     * @return The parametrized Gson object
     */
    public static Gson buildGson() {
        // Initialize a Gson builder
        GsonBuilder gsonBuilder = new GsonBuilder();

        // Parametrize the Gson builder
        gsonBuilder.enableComplexMapKeySerialization();

        RuntimeTypeAdapterFactory<Target> factory = RuntimeTypeAdapterFactory.of(Target.class, "type")
            .registerSubtype(Packet.class, "Packet")
            .registerSubtype(Destination.class, "Destination")
            .registerSubtype(ChargingStation.class, "ChargingStation");
        gsonBuilder.registerTypeAdapterFactory(factory);

        gsonBuilder.registerTypeAdapter(Optional.class, new OptionalDeserializer<>());

        // Create the Gson object and return it
        return gsonBuilder.create();   
    }
}

