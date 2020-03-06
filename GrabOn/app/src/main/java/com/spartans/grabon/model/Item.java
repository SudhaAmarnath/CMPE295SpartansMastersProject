package com.spartans.grabon.model;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-02-22
 */
public class Item {

    private long itemID;
    private String itemName;
    private String itemDescription;
    private String itemSellerUID;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;

    public Item() {

    }

    public Item(long itemID, String itemName, String itemDescription, String itemSellerUID, float itemPrice, String itemImage) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;

    }

    public Item(long itemID, String itemName, String itemDescription, String itemSellerUID, float itemPrice, ArrayList itemImageList) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImageList = itemImageList;

    }

    public Item(long itemID, String itemName, String itemDescription, float itemPrice, String itemImage) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;

    }

    public Item(long itemID, String itemName, String itemDescription, float itemPrice, ArrayList itemImageList) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemImageList = itemImageList;

    }

    public long getItemID() {
        return itemID;
    }

    public void setItemID(long itemID) {
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

    public String getItemSellerUID() {
        return itemSellerUID;
    }

    public void setItemSellerUID(String itemSellerUID) {
        this.itemSellerUID = itemSellerUID;
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
