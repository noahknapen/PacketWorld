package util.assignments.gson;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import java.util.Optional;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OptionalDeserializer<T> implements JsonSerializer<Optional<T>>, JsonDeserializer<Optional<T>> {
    @Override
    public Optional<T> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
        final T value = context.deserialize(json, ((ParameterizedType) typeOfT).getActualTypeArguments()[0]);
        return Optional.ofNullable(value);
    }

    @Override
    public JsonElement serialize(Optional<T> src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.orElse(null));
    }
}