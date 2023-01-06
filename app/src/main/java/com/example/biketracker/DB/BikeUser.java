package com.example.biketracker.DB;

import org.bson.types.ObjectId;

import java.util.ArrayList;

public class BikeUser {

    private ObjectId id;
    private String name;
    private String email;
    private String password;
    public ArrayList<ObjectId> groupIds = new ArrayList<>();
    public ArrayList<ArrayList<String>> checkPoints = new ArrayList<>();

    public BikeUser() {
    }

    public BikeUser(ObjectId id, String name, String email, String password, ArrayList<ObjectId> groupIds, ArrayList<ArrayList<String>> checkPoints) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.groupIds = groupIds;
        this.checkPoints = checkPoints;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<ObjectId> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(ArrayList<ObjectId> groupIds) {
        this.groupIds = groupIds;
    }

    public ArrayList<ArrayList<String>> getcheckPoints() {
        return checkPoints;
    }

    public void setcheckPoints(ArrayList<ArrayList<String>> checkPoints) {
        this.checkPoints = checkPoints;
    }
}
