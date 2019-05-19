package com.example.loknath.locationtracker.dto;

import java.io.Serializable;

public class UserDto implements Serializable {
    public String Name;
    public String key;
    public boolean status;
    public String token;
    public LocationDto location;


    @Override
    public String toString() {
        return "UserDto{" +
                "email='" + Name + '\'' +
                ", key='" + key + '\'' +
                "uStatus='" + status + '\'' +
                 '}';
    }
}
