package util.assignments.targets;

import environment.Coordinate;

import java.awt.Color;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A class that represents a packet
 */
@JsonIgnoreProperties(value={"color"})
public class Packet extends Target {

    private int rgbColor;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    @JsonCreator
    public Packet(@JsonProperty("coordinate") Coordinate coordinate,  @JsonProperty("rgbColor") int rgbColor) {
        super(coordinate);
        this.setRgbColor(rgbColor);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public int getRgbColor() {
        return rgbColor;
    }

    public Color getColor() {
        return new Color(rgbColor);
    }

    public void setRgbColor(int rgbColor) {
        this.rgbColor = rgbColor;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s", super.toString(), rgbColor);
    }

    @Override
    public boolean equals(Object object) {
        return super.equals(object);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}

