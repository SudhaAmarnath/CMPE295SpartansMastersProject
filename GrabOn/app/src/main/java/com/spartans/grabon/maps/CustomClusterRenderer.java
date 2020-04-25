package com.spartans.grabon.maps;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;


/**
 * Author : Sudha Amarnath on 2020-04-25
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<MapsActivity.StringClusterItem> {

    private final IconGenerator mClusterIconGenerator;
    private final Context mContext;

    public CustomClusterRenderer(Context context, GoogleMap map,
                                 ClusterManager<MapsActivity.StringClusterItem> clusterManager) {
        super(context, map, clusterManager);

        mContext = context;
        mClusterIconGenerator = new IconGenerator(mContext.getApplicationContext());
    }

    @Override protected void onBeforeClusterItemRendered(MapsActivity.StringClusterItem item,
                                                         MarkerOptions markerOptions) {

        if (MapsActivity.markerColor.equals("HUE_VIOLET")) {
            final BitmapDescriptor markerDescriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            markerOptions.icon(markerDescriptor).snippet(item.title);
        } else {
            final BitmapDescriptor markerDescriptor =
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            markerOptions.icon(markerDescriptor).snippet(item.title);
        }


    }

    @Override protected void onBeforeClusterRendered(Cluster<MapsActivity.StringClusterItem> cluster,
                                                     MarkerOptions markerOptions) {

        /*
            mClusterIconGenerator.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.ic_shopping_cart));

            mClusterIconGenerator.setTextAppearance(R.style.AppTheme);

            final Bitmap icon = mClusterIconGenerator.makeIcon(String.valueOf(cluster.getSize()));
            icon.setHeight(10);
            icon.setWidth(10);
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        */

    }
}