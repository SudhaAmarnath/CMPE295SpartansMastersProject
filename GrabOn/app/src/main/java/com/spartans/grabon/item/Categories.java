package com.spartans.grabon.item;

import java.util.ArrayList;

/**
 * Author : Sudha Amarnath on 2020-04-11
 */
public class Categories {

    public ArrayList<String> getItemCategoryList () {

        ArrayList<String> categoryNames = new ArrayList<String>();

        categoryNames.add("Appliances");
        categoryNames.add("Automobiles");
        categoryNames.add("Electronics");
        categoryNames.add("Fashion");
        categoryNames.add("Freebies");
        categoryNames.add("Furniture");
        categoryNames.add("Home & Garden");
        categoryNames.add("Movies & Music");
        categoryNames.add("Office");
        categoryNames.add("Other");
        categoryNames.add("Sports");
        categoryNames.add("Toys & Games");

        return categoryNames;

    }

    public ArrayList<String> getItemCategoryResource (String itemCategory) {

        ArrayList<String> categoryResource = new ArrayList<String>();

        switch (itemCategory) {
            case "Appliances":
                categoryResource.add("ic_cat_appliances");
                categoryResource.add("md_orange_500");
                break;
            case "Automobiles":
                categoryResource.add("ic_cat_automobiles");
                categoryResource.add("md_red_200");
                break;
            case "Electronics":
                categoryResource.add("ic_cat_electronics");
                categoryResource.add("md_purple_300");
                break;
            case "Fashion":
                categoryResource.add("ic_cat_fashion");
                categoryResource.add("md_pink_200");
                break;
            case "Freebies":
                categoryResource.add("ic_cat_freebies");
                categoryResource.add("colorPrimary");
                break;
            case "Furniture":
                categoryResource.add("ic_cat_furniture");
                categoryResource.add("md_brown_200");
                break;
            case "Home & Garden":
                categoryResource.add("ic_cat_home");
                categoryResource.add("md_blue_grey_200");
                break;
            case "Movies & Music":
                categoryResource.add("ic_cat_movies");
                categoryResource.add("md_lime_200");
                break;
            case "Office":
                categoryResource.add("ic_cat_office");
                categoryResource.add("md_teal_200");
                break;
            case "Sports":
                categoryResource.add("ic_cat_sports");
                categoryResource.add("md_amber_200");
                break;
            case "Toys & Games":
                categoryResource.add("ic_cat_toys");
                categoryResource.add("md_light_blue_100");
                break;
            default:
                categoryResource.add("ic_cat_other");
                categoryResource.add("md_grey_400");
                break;
        }

        return categoryResource;

    }
}
