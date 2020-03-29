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
}
