package com.example.loknath.locationtracker.dto;

import java.io.Serializable;

public class RequestDto implements Serializable {
    public String receiver;
    public String sender;

    @Override
    public String toString() {
        return "RequestDto{" +
                "receiver='" + receiver + '\'' +
                ", sender='" + sender + '\'' +
                '}';
    }
}
