package ca.agoldfish.td_project_lazarus;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 */
public class FilterFragment extends Fragment {


    //shared Prefs
    public static final String SHARED_PREFES ="sharedPrefs";

    public static final String HOUSE_CHECK = "houseCheck";
    public static final String OFFICE_CHECK = "officeCheck";
    public static final String LAND_CHECK = "landCheck";
    public static final String RETAIL_CHECK = "retailCheck";
    public static final String STORAGE_CHECK = "storageCheck";

    public static final String MIN_VALUE = "minValue";
    public static final String MAX_VALUE = "maxValue";
    public static final String SORT_TYPE = "sortType";
    public static final String SORT_ORDER = "sortOrder";
    public static final String SPEC_SORT_TYPE = "specSortType";


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

    // stay on this page if no values found
    //send message nothing found
    //globals
    CheckBox houseCB;
    CheckBox officeCB;
    CheckBox landCB;
    CheckBox retailCB;
    CheckBox storageCB;

    //spinner
    Spinner minSpinner;
    Spinner maxSpinner;
    Spinner sortSpinner;
    Spinner specSSpinner;

    //switch
    Switch sortType;

    //buttons
    Button listviewBtn;
    Button mapviewBtn;
    Button mixviewBtn;

    //Txt
    TextView specSTxt;

    List<String> minValues;
    List<String> maxValues;
    List<Double> minValuesIndex;
    List<Double> maxValuesIndex;

    List<String> sortValues;

    List<String> specSValues;
    List<String> specSValuesOriginal = new ArrayList<String>();

    //map for long names from short names
    HashMap<String,String> bigStateNames = new HashMap<String,String>();

    double minValueG;
    double maxValueG;
    String specSortType = "";

    private static final String TAG = "FilterActivity";
    private DatabaseReference mDatabase;
    private ArrayList<PropertyPackage> properties = new ArrayList<>();

    View view;


    public FilterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_filter, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);//disable rotation


        NavigationView navigationView = getActivity().findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(false);
        navigationView.getMenu().getItem(1).setChecked(false);
        navigationView.getMenu().getItem(2).setChecked(false);
        navigationView.getMenu().getItem(3).setChecked(false);
        navigationView.getMenu().getItem(4).getSubMenu().getItem(0).setChecked(true);

        //load shared prefs if it is there
        loadSharedPrefs();
        //get Firebase db
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //set up variables
        houseCB = view.findViewById(R.id.houseCb);
        officeCB = view.findViewById(R.id.officeCb);
        landCB = view.findViewById(R.id.landCb);
        retailCB = view.findViewById(R.id.retailCb);
        storageCB = view.findViewById(R.id.storageCb);

        minSpinner = view.findViewById(R.id.minSpin);
        maxSpinner = view.findViewById(R.id.maxSpin);
        sortSpinner = view.findViewById(R.id.sortSpin);
        specSTxt = view.findViewById(R.id.ssTxt);
        specSTxt.setVisibility(View.GONE);
        specSSpinner = view.findViewById(R.id.ssSpinner);
        specSSpinner.setVisibility(View.GONE);

        sortType = view.findViewById(R.id.ascOrDescSwitch);

        listviewBtn = view.findViewById(R.id.listBtn);
        mapviewBtn = view.findViewById(R.id.mapBtn);
        mixviewBtn = view.findViewById(R.id.mixBtn);

        //set up values for spinner
        minValues = new ArrayList<String>();
        minValuesIndex = new ArrayList<Double>();

        minValuesIndex.add(0.0);
        minValues.add("$0");
        long value = 0;
        for (int i = 1; i < 51; ++i) {
            if (i <= 20) {
                minValues.add(String.format("$%,d", i * 25000));
                minValuesIndex.add(i * 25000.0);
            } else if (i <= 30) {
                value = (20 * 25000) + (i % 20) * 50000;
                minValues.add(String.format("$%,d", value));
                minValuesIndex.add(value +0.0);
            } else {
                value = (20 * 25000) + (10 * 50000) + (i % 30) * 500000;
                minValues.add(String.format("$%,d", value));
                minValuesIndex.add(value +0.0);
            }
        }

        ArrayAdapter<String> minAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, minValues);
        minAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minSpinner.setAdapter(minAdapter);

        maxValues = new ArrayList<String>();
        maxValuesIndex = new ArrayList<Double>();

        maxValues.add("Unlimited");
        maxValuesIndex.add(999999999.0);
        for (int i = 1; i < 51; ++i) {
            if (i <= 20) {
                maxValues.add(String.format("$%,d", i * 25000));
                maxValuesIndex.add(i * 25000.0);
            } else if (i <= 30) {
                value = (20 * 25000) + (i % 20) * 50000;
                maxValues.add(String.format("$%,d", value));
                maxValuesIndex.add(value+0.0);
            } else {
                value = (20 * 25000) + (10 * 50000) + (i % 30) * 500000;
                maxValues.add(String.format("$%,d", value));
                maxValuesIndex.add(value+0.0);
            }
        }
        ArrayAdapter<String> maxAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, maxValues);
        maxAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        maxSpinner.setAdapter(maxAdapter);

        sortValues = new ArrayList<String>();
        sortValues.add("None");
        sortValues.add("Distance");
        sortValues.add("Price");
        sortValues.add("State");
        sortValues.add("Size");

        ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, sortValues);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(sortAdapter);

        specSValues = new ArrayList<String>();
        bigStateNames.put("CT", "Connecticut");
        bigStateNames.put("DE", "Delaware");
        bigStateNames.put("FL", "Florida");
        bigStateNames.put("MA", "Massachusetts");
        bigStateNames.put("ME", "Maine");
        bigStateNames.put("NC", "North Carolina");
        bigStateNames.put("NH", "New Hampshire");
        bigStateNames.put("NJ", "New Jersey");
        bigStateNames.put("NY", "New York");
        bigStateNames.put("PA", "Pennsylvania");
        bigStateNames.put("SC", "South Carolina");
        bigStateNames.put("VA", "Virginia");
        bigStateNames.put("VT", "Vermont");


        updateView();

        //create checkbox Listeners
        houseCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSpecSection();
            }
        });

        officeCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSpecSection();
            }
        });

        storageCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSpecSection();
            }
        });

        landCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSpecSection();
            }
        });

        retailCB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshSpecSection();
            }
        });

        //create button listeners
        listviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(0);
            }
        });
        mapviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(1);
            }
        });
        mixviewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getData(2);
            }
        });

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // your code here
                switch(position){
                    case 0: specSSpinner.setVisibility(View.GONE);
                        specSTxt.setVisibility(View.GONE);
                    break;
                    case 1: specSSpinner.setVisibility(View.GONE);
                        specSTxt.setVisibility(View.GONE);

                        break;
                    case 2: specSSpinner.setVisibility(View.GONE);
                        specSTxt.setVisibility(View.GONE);

                        break;
                    case 3:
                        specSTxt.setText("Specific State");
                        specSTxt.setVisibility(View.VISIBLE);
                        refreshSpecSection();
                        specSSpinner.setVisibility(View.VISIBLE);

                        break;
                    default:
                            specSSpinner.setVisibility(View.GONE);
                        specSTxt.setVisibility(View.GONE);

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }

        });

        //create button listeners
        sortType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sortOrderPref) {
                    sortType.setText("Descending");
                    sortOrderPref = !sortOrderPref;
                }
                else {
                    sortType.setText("Ascending");
                    sortOrderPref = !sortOrderPref;
                }
            }
        });

        return view;
    }

    public void sortType(View view) {
        if (sortType.isChecked()) {
            sortType.setText("Descending");
        } else {
            sortType.setText("Ascending");
        }
    }

    //helpers
    private double getDoubleFromMoney(String value) {
        //check between min and max values
        String parseMoney = value.replaceAll("[^\\d.]+", "");
        return Double.parseDouble(parseMoney);
    }

    private void getData(final int window) {

        final double minValue;
        final double maxValue;

        if (minSpinner.getSelectedItemPosition() != 0) {
            minValue = getDoubleFromMoney((String) minSpinner.getSelectedItem());
            minValueG = minValue;
        } else {
            minValue = 0;
            minValueG = 0;
        }

        if (maxSpinner.getSelectedItemPosition() != 0) {
            maxValue = getDoubleFromMoney((String) maxSpinner.getSelectedItem());
            maxValueG = maxValue;
        } else {
            maxValue = 999999999;
            maxValueG = 999999999;
        }


        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                properties.clear();

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if(houseCB.isChecked()&&item.getKey().equals("Homes")||
                            retailCB.isChecked()&&item.getKey().equals("Retails")||
                            landCB.isChecked()&&item.getKey().equals("Lands")||
                            officeCB.isChecked()&&item.getKey().equals("Offices")||
                            storageCB.isChecked() && item.getKey().equals("Storages"))
                        for (DataSnapshot propertyListItem : item.getChildren()) {
                            if (propertyListItem.getKey() != "") {
                                PropertyPackage property = propertyListItem.getValue(PropertyPackage.class);

                                //check between min and max values
                                double currentValue = getDoubleFromMoney(property.getPrice());
                                if (currentValue > minValue && currentValue < maxValue) {
                                    if(specSortType.equals(""))
                                        properties.add(property);
                                    else if(specSortType.equals(property.getState()))
                                        properties.add(property);
                                }//else don't add to the list
                            }
                        }
                }
                initRecyclerView(window);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }



    //wait for data
    private void initRecyclerView(int window) {

        //sort field before sending
        switch ((String) sortSpinner.getSelectedItem()) {

            case "None":
                specSortType="";
                break;
            case "Distance":
                specSortType="";
                    break;
            case "Price":
                specSortType="";
                Collections.sort(properties, new CustomPriceComparator());
                if(sortType.isChecked()){
                    Collections.reverse(properties);
                }

                break;
            case "State":
                Collections.sort(properties, new CustomStateComparator());
                if(sortType.isChecked()){
                    Collections.reverse(properties);
                }
                break;
            case "Size":
                specSortType="";
                Collections.sort(properties, new CustomSizeComparator());
                if(sortType.isChecked()){
                    Collections.reverse(properties);
                }
                break;
        }

        if (properties.size() == 0) {
            Toast.makeText(getActivity(), "Sorry nothing found :c", Toast.LENGTH_SHORT).show();
        } else {

            //save shared prefs
            saveSharedPrefs();
            FragmentChangeListener fc;
            switch (window){
                case 0:
                    Fragment listf = new ListFragment();
                    fc=(FragmentChangeListener)getActivity();
                    fc.replaceFragment(listf);
                    break;
                case 1:
                    Fragment mapf= new MapFragment();
                    fc=(FragmentChangeListener)getActivity();
                    fc.replaceFragment(mapf);
                    break;
                case 2:
                    Fragment mixF = new MixFragment();
                    fc=(FragmentChangeListener)getActivity();
                    fc.replaceFragment(mixF);
                    break;
            }

        }
    }

    private void refreshSpecSection(){

        final double minValue;
        final double maxValue;

        if (minSpinner.getSelectedItemPosition() != 0) {
            minValue = getDoubleFromMoney((String) minSpinner.getSelectedItem());
            minValueG = minValue;
        } else {
            minValue = 0;
            minValueG = 0;
        }

        if (maxSpinner.getSelectedItemPosition() != 0) {
            maxValue = getDoubleFromMoney((String) maxSpinner.getSelectedItem());
            maxValueG = maxValue;
        } else {
            maxValue = 999999999;
            maxValueG = 999999999;
        }
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                properties.clear();

                for (DataSnapshot item : dataSnapshot.getChildren()) {
                    if(houseCB.isChecked()&&item.getKey().equals("Homes")||
                            retailCB.isChecked()&&item.getKey().equals("Retails")||
                            landCB.isChecked()&&item.getKey().equals("Lands")||
                            officeCB.isChecked()&&item.getKey().equals("Offices")||
                            storageCB.isChecked() && item.getKey().equals("Storages"))
                        for (DataSnapshot propertyListItem : item.getChildren()) {
                            if (propertyListItem.getKey() != "") {
                                PropertyPackage property = propertyListItem.getValue(PropertyPackage.class);

                                //check between min and max values
                                double currentValue = getDoubleFromMoney(property.getPrice());
                                if (currentValue > minValue && currentValue < maxValue) {
                                    properties.add(property);
                                }//else don't add to the list
                            }
                        }
                }

                Collections.sort(properties, new CustomStateComparator());//sort collection

                specSValues.clear();
                specSValuesOriginal.clear();
                String PreviousValue = "";
                specSValues.add("All States");
                specSValuesOriginal.add("");
                for(PropertyPackage pp : properties){
                    if(!PreviousValue.equals(pp.getState())) {
                        PreviousValue = pp.getState();
                        specSValuesOriginal.add(pp.getState());
                        specSValues.add(bigStateNames.get(pp.getState()));
                    }
                }
                ArrayAdapter<String> sortAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_item, specSValues);
                sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                specSSpinner.setAdapter(sortAdapter);

                if(!specSortTypePref.equals("")){
                    int specSortIndex = specSValuesOriginal.indexOf(specSortTypePref);
                    if(specSortIndex>0)
                        specSSpinner.setSelection(specSortIndex);
                    else
                        specSSpinner.setSelection(0);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        specSSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                specSortType = specSValuesOriginal.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    private void saveSharedPrefs(){
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(SHARED_PREFES,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(HOUSE_CHECK,houseCB.isChecked());
        editor.putBoolean(OFFICE_CHECK,officeCB.isChecked());
        editor.putBoolean(LAND_CHECK,landCB.isChecked());
        editor.putBoolean(RETAIL_CHECK,retailCB.isChecked());
        editor.putBoolean(STORAGE_CHECK,storageCB.isChecked());
        editor.putString(MIN_VALUE, minValueG+"");
        editor.putString(MAX_VALUE, maxValueG+"");
        editor.putString(SORT_TYPE,(String) sortSpinner.getSelectedItem());
        editor.putBoolean(SORT_ORDER, sortType.isChecked());
        editor.putString(SPEC_SORT_TYPE, specSortType);
        editor.apply();
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

    private void updateView(){

        houseCB.setChecked(housePref);
        officeCB.setChecked(officePref);
        landCB.setChecked(landPref);
        retailCB.setChecked(reatilPref);
        storageCB.setChecked(stoagePref);

        int index =0;
        try{
            double minDouble = Double.parseDouble(minPref);
            index = minValuesIndex.indexOf(minDouble);
        }catch (Exception ex){

        }
        minSpinner.setSelection(index);
        index = 0;
        try{
            double maxDouble = Double.parseDouble(maxPref);
            index = maxValuesIndex.indexOf(maxDouble);
        }catch (Exception ex){

        }
        maxSpinner.setSelection(index);

        index =0;
        try{
            index = sortValues.indexOf(sortTypePref);
        }catch (Exception ex){

        }
        sortSpinner.setSelection(index);

        sortType.setChecked(sortOrderPref);
        if(sortOrderPref)
            sortType.setText("Descending");
        else
            sortType.setText("Ascending");

    }

}
