package ca.agoldfish.td_project_lazarus;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

class ClusterMarkerRender extends DefaultClusterRenderer<ClusterMarker> {

    private Context mContext;


    public ClusterMarkerRender(Context context, GoogleMap map, ClusterManager<ClusterMarker> clusterManager) {
        super(context, map, clusterManager);
        mContext = context;
    }

    @Override
    protected void onBeforeClusterItemRendered(ClusterMarker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(item.getIconImage()));
    }

}
