package com.spartans.grabon.model;

/**
 * Author : Sudha Amarnath on 2020-04-11
 */
public class ItemCategory {

    private String categoryName;
    private int categoryIconId;
    private int categoryIconColor;

    public ItemCategory () {

    }

    public ItemCategory (String categoryName, int categoryIconId, int categoryIconColor) {
        this.categoryName = categoryName;
        this.categoryIconId = categoryIconId;
        this.categoryIconColor = categoryIconColor;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getCategoryIconId() {
        return categoryIconId;
    }

    public void setCategoryIconId(int categoryIconId) {
        this.categoryIconId = categoryIconId;
    }

    public int getCategoryIconColor() {
        return categoryIconColor;
    }

    public void setCategoryIconColor(int categoryIconColor) {
        this.categoryIconColor = categoryIconColor;
    }

}
