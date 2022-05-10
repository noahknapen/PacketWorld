package util.assignments.targets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import environment.Coordinate;

import java.awt.*;

/**
 * A class representing a target
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Packet.class),
    @JsonSubTypes.Type(value = Destination.class),
    @JsonSubTypes.Type(value = ChargingStation.class)
})
public abstract class Target {

    private Coordinate coordinate;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    @JsonCreator
    public Target(@JsonProperty("coordinate") Coordinate coordinate) {
        this.setCoordinate(coordinate);
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////
    
    public Coordinate getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public void setPriority(boolean priority) {}

    public int getRgbColor() {return 0;}

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s", coordinate);
    }

    @Override
    public boolean equals(Object object) {
        boolean result = false;

        if(object instanceof Target target) {
            result = this.coordinate.equals(target.getCoordinate());
        }

        return result;
    }

    @Override
    public int hashCode() {
        return coordinate.hashCode();
    }
}
