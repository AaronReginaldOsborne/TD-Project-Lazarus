package ca.agoldfish.td_project_lazarus;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener ,FragmentChangeListener {

    NFCManager nfcManager;
    private DatabaseReference mDatabase;
    private ArrayList<PropertyPackage> properties = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        navigationView.getMenu().getItem(0).setChecked(true);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);//enable rotation

        if(savedInstanceState == null) {
            HomeFragment hf = new HomeFragment();
            FragmentTransaction tf = getSupportFragmentManager().beginTransaction();
            tf.replace(R.id.fragment_container, hf);
            tf.commit();
        }

        //set up firebase
        mDatabase = FirebaseDatabase.getInstance().getReference();


        nfcManager = new NFCManager(this);
        nfcManager.onActivityCreate();
        nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
            @Override
            public void onTagRead(String tagRead) {
                tagRead = tagRead.replace("_", " ");
                Toast.makeText(MainActivity.this,"TAG READ: " + tagRead, Toast.LENGTH_LONG).show();
                getPropertyPackageFromAddress(tagRead);
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FragmentChangeListener fc = this;
        if (id == R.id.nav_home) {
            fc.replaceFragment(new HomeFragment());
        } else if (id == R.id.nav_list) {
            fc.replaceFragment(new ListFragment());
        } else if (id == R.id.nav_map) {
            fc.replaceFragment(new MapFragment());
        } else if (id == R.id.nav_mix) {
            fc.replaceFragment(new MixFragment());
        } else if (id == R.id.nav_filter) {
            fc.replaceFragment(new FilterFragment());
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setCustomAnimations(R.anim.enter_right, R.anim.start_right,R.anim.exit_left, R.anim.start_left);
        fragmentTransaction.replace(R.id.fragment_container, fragment, fragment.toString());
        fragmentTransaction.commit();
    }

    public void backClicked(View view) {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        nfcManager.readTag(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcManager.onActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcManager.onActivityResume();
    }

    private void getPropertyPackageFromAddress(String address){

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                properties.clear();
                DataSnapshot homesFirebase = dataSnapshot.child("Homes");
                for (DataSnapshot homeFirebase : homesFirebase.getChildren()) {
                    if (homeFirebase.getKey() != "") {
                        PropertyPackage home = homeFirebase.getValue(PropertyPackage.class);
                        home.setType("home");
                        //check between min and max values
                                properties.add(home);
                    }
                }

                DataSnapshot retailsFirebase = dataSnapshot.child("Retails");
                for (DataSnapshot retailFirebase : retailsFirebase.getChildren()) {
                    if (retailFirebase.getKey() != "") {
                        PropertyPackage retail = retailFirebase.getValue(PropertyPackage.class);
                        retail.setType("retail");
                        //check between min and max values
                                properties.add(retail);
                    }
                }

                DataSnapshot landsFirebase = dataSnapshot.child("Lands");
                for (DataSnapshot landFirebase : landsFirebase.getChildren()) {
                    if (landFirebase.getKey() != "") {
                        PropertyPackage land = landFirebase.getValue(PropertyPackage.class);
                        land.setType("land");
                        //check between min and max values
                                properties.add(land);
                    }
                }

                DataSnapshot officesFirebase = dataSnapshot.child("Offices");
                for (DataSnapshot officeFirebase : officesFirebase.getChildren()) {
                    if (officeFirebase.getKey() != "") {
                        PropertyPackage office = officeFirebase.getValue(PropertyPackage.class);
                        office.setType("office");

                                properties.add(office);
                    }
                }


                DataSnapshot storagesFirebase = dataSnapshot.child("Storages");
                for (DataSnapshot storageFirebase : storagesFirebase.getChildren()) {
                    if (storageFirebase.getKey() != "") {
                        PropertyPackage storage = storageFirebase.getValue(PropertyPackage.class);
                        storage.setType("storage");
                        //check between min and max values
                                properties.add(storage);
                    }
                }

                for (PropertyPackage property: properties) {
                    if(property.getAddress().equals(address)) {
                        Intent intent = new Intent(MainActivity.this, property_information.class);
                        intent.putExtra("parcel_data", property);
                        startActivity(intent);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
