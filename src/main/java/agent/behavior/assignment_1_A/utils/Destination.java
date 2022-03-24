package agent.behavior.assignment_1_A.utils;

import environment.Coordinate;

import java.awt.*;

import com.google.gson.Gson;

/**
 * A class representing a destination
 */
public class Destination {

    private Coordinate coordinate;
    private Color color;

    ////////////////
    // CONSTRUCTOR//
    ////////////////

    public Destination(Coordinate coordinate, Color color) {
        this.coordinate = coordinate;
        this.color = color;
    }

    ////////////
    // GETTERS//
    ////////////
    
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public Color getColor() {
        return color;
    }

    //////////////
    // OVERRIDES//
    //////////////

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Destination) {
            Destination destination = (Destination) object;
            result = destination.getCoordinate().equals(this.coordinate) && destination.getColor().equals(this.color);
        }

        return result;
    }

    //////////
    // JSON //
    //////////

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Task fromJson(String taskString) {
        Gson gson = new Gson();
        return gson.fromJson(taskString, Task.class);
    }
}
