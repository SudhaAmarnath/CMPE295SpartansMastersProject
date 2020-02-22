package com.spartans.grabon.model;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-02-22
 */
public class Order {

    private long orderID;
    private String userID;
    private ArrayList<Item> items;

    public Order (long orderID, String userID, ArrayList<Item> items) {

        this.orderID = orderID;
        this.userID = userID;
        this.items = items;

    }

    public long getOrderID() {
        return orderID;
    }

    public void setOrderID(long orderID) {
        this.orderID = orderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }
}
