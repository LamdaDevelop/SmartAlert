package com.galeos.smartalert;


public class Notifications {
    String emergency;

    public Notifications(String emergency, String location, String timestamp, String instructions) {
        this.emergency = emergency;
        this.location = location;
        this.timestamp = timestamp;
        this.instructions = instructions;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    String location;
    String timestamp;
    String instructions;

}
