package util.assignments.targets;

import java.util.Optional;

import environment.Coordinate;

/**
 * A class that represents a charging station
 */
public class ChargingStation extends Target{

    private boolean inUse;
    private Optional<Integer> batteryOfUser;
    
    //////////////////
    // CONSTRUCTORS //
    //////////////////

    public ChargingStation(Coordinate coordinate) {
        super(coordinate);
        this.setInUse(false);
        this.setBatteryOfUser(Optional.empty());
    }

    public ChargingStation(Coordinate coordinate, boolean inUse, Optional<Integer> batteryOfUser) {
        super(coordinate);
        this.setInUse(inUse);
        this.setBatteryOfUser(batteryOfUser);
    }

    ///////////////
    // OVERRIDES //
    ///////////////

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

    @Override
    public String toString() {
        String string = super.toString() + " " + inUse + " " + batteryOfUser.orElse(-1);
        return string;
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
