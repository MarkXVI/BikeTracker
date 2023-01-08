package com.example.biketracker.DB.Schemas;

import org.bson.types.ObjectId;

public class Device {
    private ObjectId id;
    private String name;

    public Device() {
    }

    public Device(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
