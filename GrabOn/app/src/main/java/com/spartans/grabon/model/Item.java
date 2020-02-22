package com.spartans.grabon.model;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-02-22
 */
public class Item {

    private String itemID;
    private String itemName;
    private String itemDescription;
    private String itemSellerID;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;

    public Item() {

    }

    public Item(String itemID, String itemName, String itemDescription, String itemSellerID, float itemPrice, String itemImage) {

    }

    public Item(String itemID, String itemName, String itemDescription, String itemSellerID, float itemPrice, ArrayList itemImageList) {

    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemSellerID() {
        return itemSellerID;
    }

    public void setItemSellerID(String itemSellerID) {
        this.itemSellerID = itemSellerID;
    }

    public float getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(float itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public ArrayList getItemImageList() {
        return itemImageList;
    }

    public void setItemImageList(ArrayList itemImageList) {
        this.itemImageList = itemImageList;
    }

}
