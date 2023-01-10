package com.example.biketracker.DB;

import org.bson.types.ObjectId;

public class Device {
    private ObjectId id;
    private String name;
    private String yggio_id;

    public Device() {
    }

    public Device(ObjectId id, String name, String yggio_id) {
        this.id = id;
        this.name = name;
        this.yggio_id = yggio_id;
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

    public String getYggio_id() {
        return yggio_id;
    }

    public void setYggio_id(String yggio_id) {
        this.yggio_id = yggio_id;
    }
}
