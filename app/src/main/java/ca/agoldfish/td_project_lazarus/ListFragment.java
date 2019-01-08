package ca.agoldfish.td_project_lazarus;


import android.app.ListActivity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
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


public class ListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabase;
    private ArrayList<PropertyPackage> properties = new ArrayList<>();
    private GoogleApiClient googleApiClient;
    LatLng loc = new LatLng(42.9834702,-81.2508423);

    ImageView loadingGif;
    TextView loadingTxt;
    RecyclerView recyclerView;

    Boolean housePref;
    Boolean officePref;
    Boolean landPref;
    Boolean reatilPref;
    Boolean stoagePref;

    String minPref;
    String maxPref;
    String sortTypePref;
    Boolean sortOrderPref;
    String specSortTypePref;
    View view;

    public ListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list, container, false);

        loadingGif = view.findViewById(R.id.gifIV);
        Glide.with(this)
                .load(R.drawable.loading)
                .into(loadingGif);
        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(1).setChecked(true);
        navigationView.getMenu().getItem(4).getSubMenu().getItem(0).setChecked(false);

        googleApiClient = new GoogleApiClient.Builder
                (getActivity(), this, this)
                .addApi(LocationServices.API).build();
        loadSharedPrefs();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        loadingTxt = view.findViewById(R.id.loadingTxt);
        recyclerView = view.findViewById(R.id.rv);
        initImageBitmaps();

        return view;
    }

    private void initImageBitmaps(){
        Log.d(TAG,"initImagesBitMaps: preparing bitmaps.");
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                properties.clear();
                if (housePref) {
                    DataSnapshot homesFirebase = dataSnapshot.child("Homes");
                    for (DataSnapshot homeFirebase : homesFirebase.getChildren()) {
                        if (homeFirebase.getKey() != "") {
                            PropertyPackage home = homeFirebase.getValue(PropertyPackage.class);
                            home.setType("home");
                            //check between min and max values
                            double currentValue = getDoubleFromMoney(home.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals(""))
                                    properties.add(home);
                                else if(specSortTypePref.equals(home.getState()))
                                    properties.add(home);
                            }//else don't add to the list
                        }
                    }
                }

                if (reatilPref) {
                    DataSnapshot retailsFirebase = dataSnapshot.child("Retails");
                    for (DataSnapshot retailFirebase : retailsFirebase.getChildren()) {
                        if (retailFirebase.getKey() != "") {
                            PropertyPackage retail = retailFirebase.getValue(PropertyPackage.class);
                            retail.setType("retail");
                            //check between min and max values
                            double currentValue = getDoubleFromMoney(retail.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals(""))
                                    properties.add(retail);
                                else if(specSortTypePref.equals(retail.getState()))
                                    properties.add(retail);
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
                                if(specSortTypePref.equals(""))
                                    properties.add(land);
                                else if(specSortTypePref.equals(land.getState()))
                                    properties.add(land);
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
                                if(specSortTypePref.equals(""))
                                    properties.add(office);
                                else if(specSortTypePref.equals(office.getState()))
                                    properties.add(office);
                            }//else don't add to the list
                        }
                    }
                }

                if (stoagePref) {
                    DataSnapshot storagesFirebase = dataSnapshot.child("Storages");
                    for (DataSnapshot storageFirebase : storagesFirebase.getChildren()) {
                        if (storageFirebase.getKey() != "") {
                            PropertyPackage storage = storageFirebase.getValue(PropertyPackage.class);
                            storage.setType("storage");
                            //check between min and max values
                            double currentValue = getDoubleFromMoney(storage.getPrice());
                            if (currentValue > Double.parseDouble(minPref) && currentValue < Double.parseDouble(maxPref)) {
                                if(specSortTypePref.equals(""))
                                    properties.add(storage);
                                else if(specSortTypePref.equals(storage.getState()))
                                    properties.add(storage);
                            }//else don't add to the list
                        }
                    }
                }
                //sort field before sending
                switch (sortTypePref) {
                    case "None":
                        break;
                    case "Distance":
                        Collections.sort(properties, new CustomDistanceComparator(loc));
                        if(sortOrderPref){
                            Collections.reverse(properties);
                        }
                        break;
                    case "Price":
                        Collections.sort(properties, new CustomPriceComparator());
                        if(sortOrderPref){
                            Collections.reverse(properties);
                        }

                        break;
                    case "Size":
                        Collections.sort(properties, new CustomSizeComparator());
                        if(sortOrderPref){
                            Collections.reverse(properties);
                        }
                        break;
                    case "State":
                        Collections.sort(properties, new CustomStateComparator());
                        if(sortOrderPref){
                            Collections.reverse(properties);
                        }
                        break;
                }
                initRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //wait for data
    private void initRecyclerView(){


        Log.d(TAG, "initRecyclerView: init recyclerview.");
        loadingTxt.setVisibility(View.GONE);
        loadingGif.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        RecyclerView recyclerView = view.findViewById(R.id.rv);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(),properties);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void loadSharedPrefs(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFES,MODE_PRIVATE);

        housePref = sharedPreferences.getBoolean(HOUSE_CHECK,true);
        officePref = sharedPreferences.getBoolean(OFFICE_CHECK,true);
        landPref = sharedPreferences.getBoolean(LAND_CHECK,true);
        reatilPref = sharedPreferences.getBoolean(RETAIL_CHECK,true);
        stoagePref = sharedPreferences.getBoolean(STORAGE_CHECK,true);

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
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            double lat = lastLocation.getLatitude(), lon = lastLocation.getLongitude();
            loc = new LatLng(lat, lon);
            Log.i("MyMapsPrep", loc.toString());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(ListActivity.class.getSimpleName(), "Ccnnection suspended to Google Play Services!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(ListActivity.class.getSimpleName(), "Can't connect to Google Play Services!");
    }
}
