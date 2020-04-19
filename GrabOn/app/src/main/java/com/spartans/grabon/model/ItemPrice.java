package com.spartans.grabon.model;

public class ItemPrice {
    private String value;
    private String currency;

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public ItemPrice(String value, String currency) {
        this.value = value;
        this.currency = currency;
    }
}
