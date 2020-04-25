package com.spartans.grabon.maps;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.spartans.grabon.R;

/**
 * Author : Sudha Amarnath on 2020-04-25
 */
public class CustomInfoViewAdapter implements GoogleMap.InfoWindowAdapter {

    private final LayoutInflater mInflater;

    public CustomInfoViewAdapter(LayoutInflater inflater) {
        this.mInflater = inflater;
    }

    @Override public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override public View getInfoContents(Marker marker) {
        final View popup = mInflater.inflate(R.layout.layout_marker_info, null);

        ((TextView) popup.findViewById(R.id.title)).setText(marker.getSnippet());

        return popup;
    }
}
