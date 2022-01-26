package com.example.rpicargui2022;

public class Vehicle {
    public Vehicle ()
    {
    }

    public String getMoveCommand(int _speed) {
        if (-100 < _speed || _speed > 100) {
            return ("[Vehicle]: invalid speed value!");
        }else
            return ("vehicle.move "+_speed);
    }

    public String getSteerCommand(int _direction) {
        return ("vehicle.steer "+_direction);
    }

}
