package util.assignments.targets;

import com.fasterxml.jackson.annotation.*;
import environment.Coordinate;

import java.awt.Color;

/**
 * A class representing a packet
 */
@JsonIgnoreProperties(value={"color"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        defaultImpl = Packet.class
)
public class Packet extends Target {

    // A data member holding the color of the packet
    private int rgbColor;
    // A data member holding if the packet has priority
    private boolean priority;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public Packet(Coordinate coordinate, int rgbColor) {
        super(coordinate);
        this.setRgbColor(rgbColor);
        this.setPriority(false);
    }

    @JsonCreator
    public Packet(@JsonProperty("coordinate") Coordinate coordinate,  @JsonProperty("rgbColor") int rgbColor, @JsonProperty("priority") boolean priority) {
        super(coordinate);
        this.setRgbColor(rgbColor);
        this.setPriority(priority);
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

    public boolean hasPriority() {
        return priority;
    }
    
    public void setRgbColor(int rgbColor) {
        this.rgbColor = rgbColor;
    }

    @Override
    public void setPriority(boolean priority) {
        this.priority = priority;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s", super.toString(), rgbColor, priority);
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

