package util.assignments.targets;

import java.util.Optional;

import com.fasterxml.jackson.annotation.*;

import environment.Coordinate;

/**
 * A class representing a charging station
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        defaultImpl = ChargingStation.class
)
public class ChargingStation extends Target{

    // A datamember holding if the battery station is in use or not
    private boolean inUse;
    // A datamember holding the battery of the user currently using the charging station
    private Optional<Integer> batteryOfUser;

    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ChargingStation(Coordinate coordinate) {
        super(coordinate);
        this.setInUse(false);
        this.setBatteryOfUser(Optional.empty());
    }

    @JsonCreator
    public ChargingStation(@JsonProperty("coordinate") Coordinate coordinate, @JsonProperty("inUse") boolean inUse, @JsonProperty("batteryOfUser") Optional<Integer> batteryOfUser) {
        super(coordinate);
        this.setInUse(inUse);
        this.setBatteryOfUser(batteryOfUser);
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

    public void setBatteryOfUser(Optional<Integer> batteryOfUser) {
        this.batteryOfUser = batteryOfUser;
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
