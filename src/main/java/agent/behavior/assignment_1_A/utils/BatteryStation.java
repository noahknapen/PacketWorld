package agent.behavior.assignment_1_A.utils;

import com.google.gson.Gson;

import environment.Coordinate;

public class BatteryStation {

    private Coordinate coordinate;
    
    /////////////////
    // CONSTRUCTOR //
    /////////////////

    public BatteryStation(Coordinate coordinate)
    {
        this.coordinate = coordinate;
    }

    /////////////
    // GETTERS //
    /////////////

    public Coordinate getCoordinate()
    {
        return this.coordinate;
    }

    ///////////////
    // OVERRIDES //
    ///////////////

    @Override
    public boolean equals(Object object)
    {
        boolean result = false;
        
        if (object instanceof BatteryStation)
        {
            BatteryStation batteryObject = (BatteryStation) object;
            result = this.coordinate.equals(batteryObject.getCoordinate());
        }

        return result;
    }

    //////////
    // JSON //
    /////////

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Task fromJson(String taskString)
    {
        Gson gson = new Gson();
        return gson.fromJson(taskString, Task.class);
    }
}
