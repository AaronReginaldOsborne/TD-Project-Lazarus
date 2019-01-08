package ca.agoldfish.td_project_lazarus;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import ca.agoldfish.td_vr_test.UnityPlayerActivity;

public class property_information extends AppCompatActivity implements
        OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnInfoWindowClickListener,
        View.OnFocusChangeListener, View.OnClickListener {

    NFCManager nfcManager;

    ScrollView scrollView;

    MediaPlayer mediaPlayer;

    //user Input
    TextInputLayout mNameWrapper;
    private TextInputEditText mNameEd;
    private RequiredFieldValidator mNameFieldValidator;

    TextInputLayout mPhoneWrapper;
    private TextInputEditText mPhoneEd;
    private PhoneFieldValidator mPhoneFieldValidator;

    TextInputLayout mEmailWrapper;
    private TextInputEditText mEmailEd;
    private EmailFieldValidator mEmailFieldValidator;

    TextInputLayout mSubjectWrapper;
    private TextInputEditText mSubjectEd;
    private RequiredFieldValidator mSubjectFieldValidator;

    private Button mContactBtn;

    private GoogleMap mMap;
    private PropertyPackage property;
    private GoogleApiClient googleApiClient;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_PHONE_CALL = 1;
    private static final String TAG = "prop_info_Activity";

    private VideoView videoView;

    private String ExportToNFC;

    private String phoneReal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_information);


        scrollView = findViewById(R.id.scrollView);
        scrollView.setOnTouchListener(new OnSwipeTouchListener(property_information.this) {
            public void onSwipeTop() {
            }

            public void onSwipeRight() {
                finish();
            }

            public void onSwipeLeft() {
            }

            public void onSwipeBottom() {
            }

        });

        Log.d(TAG, "onCreate: started.");
        mediaPlayer = MediaPlayer.create((this), R.raw.animewow);

        //google maps
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
        googleApiClient = new GoogleApiClient.Builder
                (this, this, this)
                .addApi(LocationServices.API).build();

        //set up userInput values
        mNameEd = findViewById(R.id.name_ed);
        mNameWrapper = findViewById(R.id.name_ed_wrapper);

        mPhoneEd = findViewById(R.id.phone_ed);
        mPhoneWrapper = findViewById(R.id.phone_ed_wrapper);

        mEmailEd = findViewById(R.id.email_ed);
        mEmailWrapper = findViewById(R.id.email_ed_wrapper);

        mSubjectEd = findViewById(R.id.subject_ed);
        mSubjectWrapper = findViewById(R.id.subject_ed_wrapper);

        mContactBtn = findViewById(R.id.contactBtn);


        //set up video

        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath("android.resource://" +
                getApplicationContext().getPackageName() +
                "/" +
                R.raw.storage);
        videoView.setMediaController(new MediaController(this));
        videoView.start();
        //set onFocus change Listener to all the EditText Views
        mNameEd.setOnFocusChangeListener(this);
        mPhoneEd.setOnFocusChangeListener(this);
        mEmailEd.setOnFocusChangeListener(this);
        mSubjectEd.setOnFocusChangeListener(this);

        //init all the validators
        mNameFieldValidator = new RequiredFieldValidator(mNameWrapper);
        mPhoneFieldValidator = new PhoneFieldValidator(mPhoneWrapper);
        mEmailFieldValidator = new EmailFieldValidator(mEmailWrapper);
        mSubjectFieldValidator = new RequiredFieldValidator(mSubjectWrapper);

        //set the sibmit button onclick Listener
        mContactBtn.setOnClickListener(this);

        // Request for persmission
        ActivityCompat.requestPermissions(this,
                new String[] {Manifest.permission.SEND_SMS},1);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CALL_PHONE}, 1);

        //NFC
        nfcManager = new NFCManager(this);
        nfcManager.onActivityCreate();

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        getIncomingIntent();


        nfcManager.setOnTagWriteListener(new NFCManager.TagWriteListener() {
            @Override
            public void onTagWritten() {
                //how to write to tag
                ExportToNFC = ExportToNFC.replace("_", " ");
                Toast.makeText(property_information.this, "Tag Written: " + ExportToNFC, Toast.LENGTH_LONG).show();
            }
        });
        nfcManager.setOnTagWriteErrorListener(new NFCManager.TagWriteErrorListener() {
            @Override
            public void onTagWriteError(NFCWriteException exception) {
                Toast.makeText(property_information.this, exception.getType().toString(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void getIncomingIntent() {
        Log.d(TAG, "getIncomingIntent: checking for incoming intents.");
        property = getIntent().getParcelableExtra("parcel_data");
        setProperty(property);

    }

    private void setProperty(PropertyPackage property) {

        ImageView propIV = findViewById(R.id.propertyImg);
        Glide.with(this)
                .asBitmap()
                .load(property.getPicture())
                .into(propIV);

        TextView addressTV = findViewById(R.id.addressTxt);
        addressTV.setText(property.getAddress());

        TextView locationTV = findViewById(R.id.subAddressTxt);
        locationTV.setText(property.getLocation());

        TextView priceTV = findViewById(R.id.priceTxt);
        priceTV.setText(property.getPrice());

        TextView realtorTV = findViewById(R.id.realtorNameTxt);
        realtorTV.setText(property.getRealtor());

        TextView companyTV = findViewById(R.id.companyNameTxt);
        companyTV.setText(property.getRealtorCompany());

        TextView phoneTV = findViewById(R.id.phoneTxt);
        phoneReal = property.getPhone();
        phoneTV.setText(property.getPhone());

        TextView bedTV = findViewById(R.id.bedTxt);
        bedTV.setText(property.getBedrooms());

        TextView bathTV = findViewById(R.id.bathTxt);
        bathTV.setText(property.getBathrooms());

        TextView sizeTV = findViewById(R.id.sizeTxt);
        sizeTV.setText(property.getSize());

        TextView descTV = findViewById(R.id.descTxt);
        descTV.setText(property.getDescription());

        ImageView ppImage = findViewById(R.id.ppTypeImg);

        //seticon
        switch (property.getType()) {
            case "home":
                //seticon
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.houseicon)
                        .into(ppImage);
                break;
            case "retail":
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.shopicon)
                        .into(ppImage);
                break;
            case "land":
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.landicon)
                        .into(ppImage);
                break;
            case "office":
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.buildingicon)
                        .into(ppImage);
                break;
            case "storage":
                Glide.with(this)
                        .asBitmap()
                        .load(R.drawable.storageicon)
                        .into(ppImage);
                break;
        }

        CardView storageVideoCV = findViewById(R.id.storageView);
        CardView arCV = findViewById(R.id.arView);


        if (property.getType().equals("storage"))
            storageVideoCV.setVisibility(View.VISIBLE);

        if (property.getArcore().equals("true"))
            arCV.setVisibility(View.VISIBLE);


        mSubjectEd.setText("I am interested in " + property.getAddress() + ",\n" + property.getLocation() + ".");


    }

    public void arClicked(View view) {
        Intent intent = new Intent(this, ArcoreActivity.class);
        startActivity(intent);
    }

    public void vrClicked(View view) {
        Intent runUnity = new Intent(this, UnityPlayerActivity.class);
        startActivity(runUnity);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i("MyMapsPrep", "Connected to Google Play Services!");

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng currentHouse = new LatLng(property.getLatitude(), property.getLongitude());
        switch (property.getType()) {
            case "home":
                mMap.addMarker(new MarkerOptions().position(currentHouse)
                        .title(property.getAddress())
                        .snippet(property.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.housemarker2)));
                break;
            case "office":
                mMap.addMarker(new MarkerOptions().position(currentHouse)
                        .title(property.getAddress())
                        .snippet(property.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.buildingmarker2)));
                break;
            case "land":
                mMap.addMarker(new MarkerOptions().position(currentHouse)
                        .title(property.getAddress())
                        .snippet(property.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.landmarker2)));
                break;
            case "retail":
                mMap.addMarker(new MarkerOptions().position(currentHouse)
                        .title(property.getAddress())
                        .snippet(property.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.shopmarker2)));
                break;
            case "storage":
                mMap.addMarker(new MarkerOptions().position(currentHouse)
                        .title(property.getAddress())
                        .snippet(property.getLocation())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.storagemarker2)));
                break;
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentHouse));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentHouse, 16));
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    protected void onStart() {
        if (googleApiClient != null) {
            googleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
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
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_ACCESS_FINE_LOCATION);
                    }
                }
            }
        }
    }

    public void backClicked(View view) {
        finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.contactBtn) {
            //d a final validation here
            String name = mNameEd.getText().toString();
            String phone = mPhoneEd.getText().toString();
            String email = mEmailEd.getText().toString();
            String subject = mSubjectEd.getText().toString();

            boolean isNameValid = mNameFieldValidator.validate(name);
            boolean isPhoneValid = mPhoneFieldValidator.validate(phone);
            boolean isEmailValid = mEmailFieldValidator.validate(email);
            boolean isSubjectValid = mSubjectFieldValidator.validate(subject);

            if (isNameValid && isPhoneValid && isEmailValid && isSubjectValid) {
                Toast.makeText(this, "Message Sent", Toast.LENGTH_SHORT).show();
                if (!mediaPlayer.isPlaying())
                    mediaPlayer.start();
            }
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            return; //we want to validate only fields loosing focus and not fields gaining focus
        }

        int id = v.getId();
        if (id == R.id.name_ed) {
            mNameFieldValidator.validate(mNameEd.getText().toString());
        } else if (id == R.id.phone_ed) {
            mPhoneFieldValidator.validate(mPhoneEd.getText().toString());
        } else if (id == R.id.email_ed) {
            mEmailFieldValidator.validate(mEmailEd.getText().toString());
        } else if (id == R.id.subject_ed) {
            mSubjectFieldValidator.validate(mSubjectEd.getText().toString());
        }
    }
    public void userClicked(View view) {
        Toast.makeText(this,"User Profile Clicked",Toast.LENGTH_SHORT).show();
    }
    public void phoneClicked(View view) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:"+phoneReal));
        if (ContextCompat.checkSelfPermission(property_information.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(property_information.this, new String[]{Manifest.permission.CALL_PHONE},REQUEST_PHONE_CALL);
        }
        else
        {
            this.startActivity(callIntent);
        }
    }

    public void mailClicked(View view) {
        //d a final validation here
        String name = mNameEd.getText().toString();
        String phone = mPhoneEd.getText().toString();;
        String email = mEmailEd.getText().toString();
        String subject = mSubjectEd.getText().toString();

        boolean isNameValid = mNameFieldValidator.validate(name);
        boolean isPhoneValid = mPhoneFieldValidator.validate(phone);
        boolean isEmailValid = mEmailFieldValidator.validate(email);
        boolean isSubjectValid = mSubjectFieldValidator.validate(subject);

        if (isNameValid && isPhoneValid && isEmailValid && isSubjectValid) {
            String fakeEmail = property.getRealtor().replaceAll(" ","");
            String [] to = {fakeEmail+"@gmail.com"};
            String [] cc = {"TD_Project_Lazarus@gmail.com"};
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setData(Uri.parse("mailto:"));
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            emailIntent.putExtra(Intent.EXTRA_CC,cc);
            emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello " + property.getRealtor() +",\n\n" +  mSubjectEd.getText().toString() +"\n\nThank you,\n"+mNameEd.getText().toString() );
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, property.getAddress());
            emailIntent.setType("message/rfc822");
            startActivity(Intent.createChooser(emailIntent,"Email"));
        }

    }

    public void smsClicked(View view) {
        //d a final validation here
        String name = mNameEd.getText().toString();
        String phone = mPhoneEd.getText().toString();
        String email = mEmailEd.getText().toString();
        String subject = mSubjectEd.getText().toString();

        boolean isNameValid = mNameFieldValidator.validate(name);
        boolean isPhoneValid = mPhoneFieldValidator.validate(phone);
        boolean isEmailValid = mEmailFieldValidator.validate(email);
        boolean isSubjectValid = mSubjectFieldValidator.validate(subject);

        if (isNameValid && isPhoneValid && isEmailValid && isSubjectValid) {
            // First launch using the sms intent
//            Intent smsIntent = new Intent(Intent.ACTION_VIEW);
//            // Localhost 5554
//            smsIntent.putExtra("address", "5554");
//            smsIntent.putExtra("sms_body", subject);
//            smsIntent.setType("vnd.android-dir/mms-sms");
//            startActivity(smsIntent);

            Uri uri = Uri.parse("smsto:"+phoneReal);
            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
            it.putExtra("sms_body", subject);
            startActivity(it);

        }
    }


    /*NFC*/
    @Override
    public void onNewIntent(Intent intent) {
        ExportToNFC = property.getAddress();
        ExportToNFC = ExportToNFC.replace(" ", "_");
        nfcManager.setWrittenData(ExportToNFC);
        nfcManager.writeDataToTag(intent,ExportToNFC);
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

}
