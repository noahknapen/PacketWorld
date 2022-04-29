package util.assignments.targets;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import environment.Coordinate;

/**
 * A class that represents a packet
 */
public class ChargingStation extends Target{

    private boolean inUse;
    private Optional<Integer> batteryOfUser;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ChargingStation(Coordinate coordinate) {
        super(coordinate);
        this.inUse = false;
        this.batteryOfUser = Optional.empty();
    }

    @JsonCreator
    public ChargingStation(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("inUse") boolean inUse, @JsonProperty("batteryOfUser") Optional<Integer> batteryOfUser) {
        super(coordinate);
        this.inUse = inUse;
        this.batteryOfUser = batteryOfUser;
    }

    ///////////////////////
    // GETTERS & SETTERS //
    ///////////////////////

    public boolean isInUse() {
        return inUse;
    }

    public Optional<Integer> getBatteryOfUser() {
        return batteryOfUser;
    }

    public void setInUse(boolean inUse) {
        this.inUse = inUse;
    }

    public void setBatteryOfUser(Optional<Integer> optional) {
        this.batteryOfUser = optional;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public String toString() {
        return String.format("%s %s %s", super.toString(), inUse, batteryOfUser);
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
