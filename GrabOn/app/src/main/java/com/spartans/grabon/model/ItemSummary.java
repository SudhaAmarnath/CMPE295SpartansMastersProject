package com.spartans.grabon.model;

import java.util.List;

public class ItemSummary {
    private String itemId;
    private String title;
    private String itemGroupHref;

    private ItemImage image;
    private ItemPrice price;

    private String itemGroupType;
    private String itemHref;

    private Seller seller;

    private String condition;
    private String conditionId;

    private List<ImageUrl> thumbnailImages;
    private List<ShippingOption> shippingOptions;
    private List<String> buyingOptions;


    private String itemAffiliateWebUrl;
    private String itemWebUrl;

    private ItemLocation itemLocation;
    private List<Category> categories;

    private boolean adultOnly;

    private int vendorID = 0;

    public String getItemId() {
        return itemId;
    }

    public String getTitle() {
        return title;
    }

    public String getItemGroupHref() {
        return itemGroupHref;
    }

    public ItemImage getImage() {
        return image;
    }

    public ItemPrice getPrice() {
        return price;
    }

    public String getItemGroupType() {
        return itemGroupType;
    }

    public String getItemHref() {
        return itemHref;
    }

    public Seller getSeller() {
        return seller;
    }

    public String getCondition() {
        return condition;
    }

    public String getConditionId() {
        return conditionId;
    }

    public List<ShippingOption> getShippingOptions() {
        return shippingOptions;
    }

    public List<String> getBuyingOptions() {
        return buyingOptions;
    }


    public String getItemAffiliateWebUrl() {
        return itemAffiliateWebUrl;
    }

    public String getItemWebUrl() {
        return itemWebUrl;
    }

    public ItemLocation getItemLocation() {
        return itemLocation;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public boolean isAdultOnly() {
        return adultOnly;
    }

    public int getVendorID() { return vendorID; }

    public ItemSummary(int vendorID, String title, ItemImage image, ItemPrice price, String itemID) {
        this.title = title;
        this.image = image;
        this.price = price;
        this.vendorID = vendorID;
        this.itemId = itemID;
    }

    public ItemSummary(int vendorID, String title, ItemImage image, String itemLink) {
        this.title = title;
        this.image = image;
        this.itemWebUrl = itemLink;
        this.vendorID = vendorID;
    }

    public ItemSummary(int vendorID, String title, String itemLink) {
        this.title = title;
        this.itemWebUrl = itemLink;
        this.vendorID = vendorID;
    }

    @Override
    public String toString() {
        return "ItemSummary{" +
                "itemId='" + itemId + '\'' +
                ", title='" + title + '\'' +
                ", itemGroupHref='" + itemGroupHref + '\'' +
                ", image=" + image +
                ", price=" + price +
                ", itemGroupType='" + itemGroupType + '\'' +
                ", itemHref='" + itemHref + '\'' +
                ", seller=" + seller +
                ", condition='" + condition + '\'' +
                ", conditionId='" + conditionId + '\'' +
                ", thumbnailImages=" + thumbnailImages +
                ", shippingOptions=" + shippingOptions +
                ", buyingOptions=" + buyingOptions +
                ", itemAffiliateWebUrl='" + itemAffiliateWebUrl + '\'' +
                ", itemWebUrl='" + itemWebUrl + '\'' +
                ", itemLocation=" + itemLocation +
                ", categories=" + categories +
                ", adultOnly=" + adultOnly +
                '}';
    }
}
