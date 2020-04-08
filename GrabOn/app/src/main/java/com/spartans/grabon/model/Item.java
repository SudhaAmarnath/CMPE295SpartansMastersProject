package com.spartans.grabon.model;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-02-22
 */
public class Item {

    private String itemID;
    private String itemName;
    private String itemDescription;
    private String itemSellerUID;
    private float  itemPrice;
    private String itemImage;
    private ArrayList itemImageList;
    private boolean itemOrdered;
    private boolean itemPicked;
    private String latitude;
    private String longitude;
    private String itemAddress;

    public Item() {

    }

    public Item(String itemID, String itemName, String itemDescription, String itemSellerUID, float itemPrice, ArrayList itemImageList) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImageList = itemImageList;

    }

    public Item(String itemName, String itemDescription, String itemSellerUID, float itemPrice, String itemImage, ArrayList itemImageList) {

        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.itemImageList = itemImageList;

    }

    public Item(String itemID, String itemName, String itemDescription, float itemPrice, String itemImage) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemSellerUID = itemSellerUID;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;

    }

    public Item(String itemID, String itemName, String itemDescription, float itemPrice, ArrayList itemImageList) {

        this.itemID = itemID;
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemPrice = itemPrice;
        this.itemImageList = itemImageList;

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

    public boolean isItemOrdered() {
        return itemOrdered;
    }

    public void setItemOrdered(boolean itemOrdered) {
        this.itemOrdered = itemOrdered;
    }

    public boolean isItemPicked() {
        return itemPicked;
    }

    public void setItemPicked(boolean itemPicked) {
        this.itemPicked = itemPicked;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getItemAddress() {
        return itemAddress;
    }

    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }
}
