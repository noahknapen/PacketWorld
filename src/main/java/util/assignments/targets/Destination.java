package util.assignments.targets;

import com.fasterxml.jackson.annotation.*;
import environment.Coordinate;

import java.awt.Color;

/**
 * A class representing a destination
 */
@JsonIgnoreProperties(value={"color"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        defaultImpl = Destination.class
)
public class Destination extends Target{
    
    // A data member holding the color of the destination
    private int rgbColor;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    @JsonCreator
    public Destination(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("rgbColor") int rgbColor) {
        super(coordinate);
        this.setRgbColor(rgbColor);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public int getRgbColor() {
        return rgbColor;
    }

    /**
     * Get the color
     * 
     * @return The color of the packet
     */
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
