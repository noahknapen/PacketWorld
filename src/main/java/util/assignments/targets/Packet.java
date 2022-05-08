package util.assignments.targets;

import com.fasterxml.jackson.annotation.*;
import environment.Coordinate;

import java.awt.Color;

/**
 * A class that represents a packet
 */
@JsonIgnoreProperties(value={"color"})
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        defaultImpl = Packet.class
)
public class Packet extends Target {

    private int rgbColor;
    private boolean prioPacket;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    @JsonCreator
    public Packet(@JsonProperty("coordinate") Coordinate coordinate,  @JsonProperty("rgbColor") int rgbColor) {
        super(coordinate);
        this.setRgbColor(rgbColor);
        this.setPrioPacket(false);
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

    public boolean isPrioPacket() {
        return prioPacket;
    }

    public void setPrioPacket(boolean prioPacket) {
        this.prioPacket = prioPacket;
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

