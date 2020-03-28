package com.spartans.grabon.model;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-02-22
 */
public class Order {

    private String orderID;
    private String userID;
    private String itemSellerUID;
    private ArrayList<Item> items;
    private double orderTotal;

    public Order (String orderID, String userID, String itemSellerUID,  ArrayList<Item> items, double orderTotal) {

        this.orderID = orderID;
        this.userID = userID;
        this.items = items;
        this.itemSellerUID = itemSellerUID;
        this.orderTotal = orderTotal;

    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getItemSellerUID() {
        return itemSellerUID;
    }

    public void setItemSellerUID(String itemSellerUID) {
        this.itemSellerUID = itemSellerUID;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal = orderTotal;
    }


}
