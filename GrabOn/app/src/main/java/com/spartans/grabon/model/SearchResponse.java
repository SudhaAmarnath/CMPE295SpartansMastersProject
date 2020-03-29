package com.spartans.grabon.model;

import java.util.List;

public class SearchResponse {
    private String href;
    private long total;
    private String next;
    private int limit;
    private int offset;

    private List<ItemSummary> itemSummaries;

    public String getHref() {
        return href;
    }

    public long getTotal() {
        return total;
    }

    public String getNext() {
        return next;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public List<ItemSummary> getItemSummaries() {
        return itemSummaries;
    }
}
