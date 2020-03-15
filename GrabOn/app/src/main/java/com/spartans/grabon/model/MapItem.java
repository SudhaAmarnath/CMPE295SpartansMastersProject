package com.spartans.grabon.model;


import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Author : Sudha Amarnath on 2020-03-15
 */
public class MapItem implements ClusterItem {


    private final LatLng mPosition;
    private String title;
    private String snippet;

    public MapItem (double latitude, double longitude, String title, String snippet) {
        mPosition = new LatLng(latitude, longitude);
        this.title = title;
        this.snippet = snippet;
    }

    public LatLng getPosition() {
        return mPosition;
    }

    public String getTitle () {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

}
