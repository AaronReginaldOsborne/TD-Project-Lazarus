package ca.agoldfish.td_project_lazarus;

import android.os.Debug;

import com.google.android.gms.maps.model.LatLng;

import java.util.Comparator;

public class CustomDistanceComparator implements Comparator<PropertyPackage> {
    LatLng currentLoc;
    double R = 6372.8; // In kilometers

    public CustomDistanceComparator(LatLng current){
        currentLoc = current;
    }
    @Override
    public int compare(final PropertyPackage place1, final PropertyPackage place2) {
        double lat1 = place1.getLatitude();
        double lon1 = place1.getLongitude();
        double lat2 = place2.getLatitude();
        double lon2 = place2.getLongitude();

        double distanceToPlace1 = distance(currentLoc.latitude, currentLoc.longitude, lat1, lon1);
        double distanceToPlace2 = distance(currentLoc.latitude, currentLoc.longitude, lat2, lon2);
        return (int) (distanceToPlace1 - distanceToPlace2);
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }
}