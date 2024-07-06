package com.example.necessity.HistoryActivities;

import java.util.ArrayList;

public class model {
    String name, type, description, userid, timestamp, fooditem;
    long expiry;
    ArrayList<String> links;

    public model() {

    }

    public model(String name, String type, String description, String userid, String timestamp, String fooditem, long expiry, ArrayList<String> links) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.userid = userid;
        this.timestamp = timestamp;
        this.fooditem = fooditem;
        this.expiry = expiry;
        this.links = links;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getFooditem() {
        return fooditem;
    }

    public long getExpiry() {
        return expiry;
    }

    public ArrayList<String> getLinks() {
        return links;
    }
}
