package com.example.biketracker.DB;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class Group {
    private ObjectId id;
    private String name;
    private ArrayList<ObjectId> deviceIds;

    public Group() {
    }

    public Group(ObjectId id, String name, ArrayList<ObjectId> deviceIds) {
        this.id = id;
        this.name = name;
        this.deviceIds = deviceIds;
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

    public ArrayList<ObjectId> getDeviceIds() {
        return deviceIds;
    }

    public void setDeviceIds(ArrayList<ObjectId> deviceIds) {
        this.deviceIds = deviceIds;
    }
}
