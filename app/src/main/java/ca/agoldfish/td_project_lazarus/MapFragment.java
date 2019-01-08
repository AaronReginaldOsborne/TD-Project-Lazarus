package ca.agoldfish.td_project_lazarus;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static android.content.Context.MODE_PRIVATE;
import static ca.agoldfish.td_project_lazarus.FilterFragment.HOUSE_CHECK;
import static ca.agoldfish.td_project_lazarus.FilterFragment.LAND_CHECK;
import static ca.agoldfish.td_project_lazarus.FilterFragment.MAX_VALUE;
import static ca.agoldfish.td_project_lazarus.FilterFragment.MIN_VALUE;
import static ca.agoldfish.td_project_lazarus.FilterFragment.OFFICE_CHECK;
import static ca.agoldfish.td_project_lazarus.FilterFragment.RETAIL_CHECK;
import static ca.agoldfish.td_project_lazarus.FilterFragment.SHARED_PREFES;
import static ca.agoldfish.td_project_lazarus.FilterFragment.SORT_ORDER;
import static ca.agoldfish.td_project_lazarus.FilterFragment.SORT_TYPE;
import static ca.agoldfish.td_project_lazarus.FilterFragment.SPEC_SORT_TYPE;
import static ca.agoldfish.td_project_lazarus.FilterFragment.STORAGE_CHECK;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,ClusterManager.OnClusterClickListener<ClusterMarker>,
        ClusterManager.OnClusterItemClickListener<ClusterMarker>,
        ClusterManager.OnClusterItemInfoWindowClickListener<ClusterMarker>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String TAG = "MapsActivity";
    private ClusterManager<ClusterMarker> mClusterManager;
    private GoogleMap mMap;
    MapView mMapView;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private GoogleApiClient googleApiClient;
    private LatLng loc;
    private DatabaseReference mDatabase;
    private ArrayList<PropertyPackage> properties = new ArrayList<>();

    private ArrayList<PropertyPackage> homes = new ArrayList<>();
    private ArrayList<PropertyPackage> retails = new ArrayList<>();
    private ArrayList<PropertyPackage> lands = new ArrayList<>();
    private ArrayList<PropertyPackage> offices = new ArrayList<>();
    private ArrayList<PropertyPackage> storages = new ArrayList<>();


    private ClusterMarker clickedPropertyMarker;
    Boolean housePref;
    Boolean officePref;
    Boolean landPref;
    Boolean retailPref;
    Boolean storagePrf;

    String minPref;
    String maxPref;
    String sortTypePref;
    Boolean sortOrderPref;
    String specSortTypePref;

    View view;
    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(2).setChecked(true);
        navigationView.getMenu().getItem(4).getSubMenu().getItem(0).setChecked(false);

        if (ContextCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        try {
            Log.d(TAG, "onCreateView: On Create");

            view = inflater.inflate(R.layout.fragment_map, container, false);
            MapsInitializer.initialize(this.getActivity());
            mMapView = (MapView) view.findViewById(R.id.map);
            mMapView.onCreate(savedInstanceState);
            if (mMap == null)
            mMapView.getMapAsync(this);
        }
        catch (InflateException e){
            Log.e(TAG, "Inflate exception");
        }
        loadSharedPrefs();





        googleApiClient = new GoogleApiClient.Builder
                (getActivity(), this, this)
                .addApi(LocationServices.API).build();


        //Grab data from fire base
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                properties.clear();
                homes.clear();
                retails.clear();
                lands.clear();
                offices.clear();
                storages.clear();
                if (housePref) {
                    DataSnapshot homesFirebase = dataSnapshot.child("Homes");
                    for (DataSnapshot homeFirebase : homesFirebase.getChildren()) {
                        if (homeFirebase.getKey() != "") {
                            PropertyPackage home = homeFirebase.getValue(PropertyPackage.class);
                            home.setType("home");
                            //check between min and max values
                            double currentValue = getDoubleFromMoney(home.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals("")) {
                                    properties.add(home);
                                    homes.add(home);
                                }
                                else if(specSortTypePref.equals(home.getState())) {
                                    properties.add(home);
                                    homes.add(home);
                                }
                            }//else don't add to the list
                        }
                    }
                }

                if (retailPref) {
                    DataSnapshot retailsFirebase = dataSnapshot.child("Retails");
                    for (DataSnapshot retailFirebase : retailsFirebase.getChildren()) {
                        if (retailFirebase.getKey() != "") {
                            PropertyPackage retail = retailFirebase.getValue(PropertyPackage.class);
                            retail.setType("retail");

                            //check between min and max values
                            double currentValue = getDoubleFromMoney(retail.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals("")) {
                                    properties.add(retail);
                                    retails.add(retail);
                                }
                                else if(specSortTypePref.equals(retail.getState())) {
                                    properties.add(retail);
                                    retails.add(retail);
                                }
                            }//else don't add to the list
                        }
                    }
                }

                if (landPref) {
                    DataSnapshot landsFirebase = dataSnapshot.child("Lands");
                    for (DataSnapshot landFirebase : landsFirebase.getChildren()) {
                        if (landFirebase.getKey() != "") {
                            PropertyPackage land = landFirebase.getValue(PropertyPackage.class);
                            land.setType("land");

                            //check between min and max values
                            double currentValue = getDoubleFromMoney(land.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals("")) {
                                    properties.add(land);
                                    lands.add(land);
                                }
                                else if(specSortTypePref.equals(land.getState())) {
                                    properties.add(land);
                                    lands.add(land);
                                }
                            }//else don't add to the list
                        }
                    }
                }

                if (officePref) {
                    DataSnapshot officesFirebase = dataSnapshot.child("Offices");
                    for (DataSnapshot officeFirebase : officesFirebase.getChildren()) {
                        if (officeFirebase.getKey() != "") {
                            PropertyPackage office = officeFirebase.getValue(PropertyPackage.class);
                            office.setType("office");

                            //check between min and max values
                            double currentValue = getDoubleFromMoney(office.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals("")) {
                                    properties.add(office);
                                    offices.add(office);
                                }
                                else if(specSortTypePref.equals(office.getState())) {
                                    properties.add(office);
                                    offices.add(office);
                                }
                            }//else don't add to the list
                        }
                    }
                }

                if (storagePrf) {
                    DataSnapshot storagesFirebase = dataSnapshot.child("Storages");
                    for (DataSnapshot storageFirebase : storagesFirebase.getChildren()) {
                        if (storageFirebase.getKey() != "") {
                            PropertyPackage storage = storageFirebase.getValue(PropertyPackage.class);
                            storage.setType("storage");

                            //check between min and max values
                            double currentValue = getDoubleFromMoney(storage.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals("")) {
                                    properties.add(storage);
                                    storages.add(storage);
                                }
                                else if(specSortTypePref.equals(storage.getState())) {
                                    properties.add(storage);
                                    storages.add(storage);
                                }
                            }//else don't add to the list
                        }
                    }
                }

                //sort field before sending
                switch (sortTypePref) {
                    case "None":
                        break;
                    case "Distance":

                        break;
                    case "Price":
                        Collections.sort(properties, new CustomPriceComparator());
                        if (sortOrderPref) {
                            Collections.reverse(properties);
                        }

                        break;
                    case "Size":
                        Collections.sort(properties, new CustomSizeComparator());
                        if (sortOrderPref) {
                            Collections.reverse(properties);
                        }
                        break;
                    case "State":
                        Collections.sort(properties, new CustomStateComparator());
                        if (sortOrderPref) {
                            Collections.reverse(properties);
                        }
                        break;
                }

                Log.d(TAG, "onCreateView: On finish grabbing firebase data");

                //run this after grabbing all the data
                addMarkers();
                drawLines();

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: starting onMapReady");

        mMap = googleMap;


        mClusterManager = new ClusterManager<ClusterMarker>(getActivity(), mMap);

        mClusterManager.setOnClusterClickListener(this);
        mClusterManager.setOnClusterItemClickListener(this);
        mClusterManager.setOnClusterItemInfoWindowClickListener(this);

        ClusterMarkerRender renderer = new ClusterMarkerRender(getActivity(), googleMap, mClusterManager);
        mClusterManager.setRenderer(renderer);

        //set up custom windows
        mClusterManager.getMarkerCollection().setOnInfoWindowAdapter(new CustomInfoWindowActivity(getActivity()));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        mMap.setInfoWindowAdapter(mClusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(mClusterManager);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("MyMapsPrep", "Connected to Google Play Services!");

        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
            loc = new LatLng(lat, lon);
            Log.i("MyMapsPrep", loc.toString());
            // Add a BLUE marker to current location and zoom
            mMap.addMarker(new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromResource(R.drawable.usericon))
                    .title("This is you").snippet("^_^"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
            // animate camera allows zoom
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 4));

        }
    }

    @Override
    public void onConnectionSuspended(int i) {
//        Log.i(MapsActivity.class.getSimpleName(), "Ccnnection suspended to Google Play Services!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//        Log.i(MapsActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }

    private void addMarkers(){
        //markers added
        //markers added
        for (int i = 0; i < homes.size(); ++i) {
            ClusterMarker newMarker = new ClusterMarker(homes.get(i),R.drawable.housemarker2);
            mClusterManager.addItem(newMarker);
        }

        for (int i = 0; i < retails.size(); ++i) {
            ClusterMarker newMarker = new ClusterMarker(retails.get(i),R.drawable.shopmarker2);
            mClusterManager.addItem(newMarker);
        }

        for (int i = 0; i < lands.size(); ++i) {
            ClusterMarker newMarker = new ClusterMarker(lands.get(i),R.drawable.landmarker2);
            mClusterManager.addItem(newMarker);
        }

        for (int i = 0; i < offices.size(); ++i) {
            ClusterMarker newMarker = new ClusterMarker(offices.get(i),R.drawable.buildingmarker2 );
            mClusterManager.addItem(newMarker);
        }

        for (int i = 0; i < storages.size(); ++i) {
            ClusterMarker newMarker = new ClusterMarker(storages.get(i),R.drawable.storagemarker2);
            mClusterManager.addItem(newMarker);
        }
    }


    private void drawLines(){

        drawLine(new LatLng(40.040669, -74.814312),
                new LatLng(40.040078, -74.820062));

        drawLine(new LatLng(40.040078, -74.820062),
                new LatLng(40.039560, -74.821591));

        drawLine(new LatLng(40.039560, -74.821591),
                new LatLng(40.038587, -74.820310));

        drawLine(new LatLng(40.038587, -74.820310),
                new LatLng(40.037782, -74.820181));

        drawLine(new LatLng(40.037782, -74.820181),
                new LatLng(40.037741, -74.820787));

        drawLine(new LatLng(40.037741, -74.820787),
                new LatLng(40.036932, -74.820733));

        drawLine(new LatLng(40.036932, -74.820733),
                new LatLng(40.036488, -74.820422));

        drawLine(new LatLng(40.036488, -74.820422),
                new LatLng(40.036373, -74.821436));

        drawLine(new LatLng(40.036373, -74.821436),
                new LatLng(40.034242, -74.819189));

        drawLine(new LatLng(40.034242, -74.819189),
                new LatLng(40.033547, -74.815735));

        drawLine(new LatLng(40.033547, -74.815735),
                new LatLng(40.035761, -74.813503));

        drawLine(new LatLng(40.035761, -74.813503),
                new LatLng(40.037993, -74.813854));

        drawLine(new LatLng(40.037993, -74.813854),
                new LatLng(40.037901, -74.814918));

        drawLine(new LatLng(40.037901, -74.814918),
                new LatLng(40.038414, -74.815750));

        drawLine(new LatLng(40.038414, -74.815750),
                new LatLng(40.039228, -74.814865));

        drawLine(new LatLng(40.039228, -74.814865),
                new LatLng(40.040669, -74.814312));
    }

    private void drawLine(LatLng start, LatLng end){
        mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(start.latitude, start.longitude),
                        new LatLng(end.latitude, end.longitude))
                .width(5)
                .color(Color.GREEN)
                .geodesic(true));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_ACCESS_FINE_LOCATION) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        onConnected(null);
                    }
                    else {
                        ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                }
            }
        }
    }

    private void loadSharedPrefs(){
        Log.d(TAG, "onCreateView: On loadSharedPrefs");

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFES,MODE_PRIVATE);

        housePref = sharedPreferences.getBoolean(HOUSE_CHECK,true);
        officePref = sharedPreferences.getBoolean(OFFICE_CHECK,true);
        landPref = sharedPreferences.getBoolean(LAND_CHECK,true);
        retailPref = sharedPreferences.getBoolean(RETAIL_CHECK,true);
        storagePrf = sharedPreferences.getBoolean(STORAGE_CHECK,true);

        minPref = sharedPreferences.getString(MIN_VALUE,"0");
        maxPref = sharedPreferences.getString(MAX_VALUE,"999999999");
        sortTypePref = sharedPreferences.getString(SORT_TYPE,"None");
        sortOrderPref = sharedPreferences.getBoolean(SORT_ORDER,false);
        specSortTypePref = sharedPreferences.getString(SPEC_SORT_TYPE,"");
    }

    //helpers
    private double getDoubleFromMoney(String value) {
        //check between min and max values
        String parseMoney = value.replaceAll("[^\\d.]+", "");
        return Double.parseDouble(parseMoney);
    }

    @Override
    public void onStart() {
        if(googleApiClient != null){
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public boolean onClusterClick(Cluster<ClusterMarker> cluster) {
        LatLngBounds.Builder builder = LatLngBounds.builder();

        Collection<ClusterMarker> values = cluster.getItems();

        for (ClusterItem item : values) {
            LatLng location = item.getPosition();
            builder.include(location);
        }

        final LatLngBounds bounds = builder.build();
        try { mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } catch (Exception error) {

        }

        return true;
    }

    @Override
    public boolean onClusterItemClick(ClusterMarker clusterMarker) {
//        Toast.makeText(getActivity(), "Info window clicked",
//                Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onClusterItemInfoWindowClick(ClusterMarker clusterMarker) {
        PropertyPackage propertyPackage = clusterMarker.getProperty();
        Intent intent = new Intent(getActivity(), property_information.class);
        intent.putExtra("parcel_data", propertyPackage);
        this.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }
    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
    @Override
    public void onDetach() {
        super.onDetach();
    }
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(getActivity(), "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(getActivity(), "portrait", Toast.LENGTH_SHORT).show();
        }
    }
}
