package ca.agoldfish.td_project_lazarus;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterItem;

public class ClusterMarker implements ClusterItem {

    private LatLng mPosition;
    private String mSnippet;
    private PropertyPackage mProperty;
    private String mAddress;
    private int iconImage;


    public ClusterMarker(double lat, double lng) {
        mPosition = new LatLng(lat, lng);
    }

    public ClusterMarker(double lat, double lng, String title, String snippet) {
        mPosition = new LatLng(lat, lng);
        mSnippet = snippet;
    }
    public ClusterMarker(PropertyPackage prop) {
        mProperty = prop;
        mPosition = new LatLng(prop.getLatitude(), prop.getLongitude());
        mAddress = prop.getAddress();
        mSnippet = prop.getLocation();
    }

    public ClusterMarker(PropertyPackage prop, int imageView) {
        mProperty = prop;
        mPosition = new LatLng(prop.getLatitude(), prop.getLongitude());
        mAddress = prop.getAddress();
        mSnippet = prop.getLocation();
        iconImage = imageView;
    }

    @Override
    public LatLng getPosition() {
        return mPosition;
    }

    @Override
    public String getTitle() {
        return mAddress;
    }

    //work around to send a cluster item to mark by sending Json String
    @Override
    public String getSnippet() {
        Gson gson = new Gson();
        String json = gson.toJson(mProperty);
        return json;
    }

    public String getAddress(){ return  mAddress;}

    public PropertyPackage getProperty() { return mProperty;}


    public int getIconImage() {
        return iconImage;
    }

    public void setIconImage(int iconImage) {
        this.iconImage = iconImage;
    }

}
