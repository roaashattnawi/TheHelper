package com.example.naser.thehelper;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomerMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

private GoogleMap mMap;
        GoogleApiClient mGoogleApiClient;
        LocationRequest mLocationRequest;
        Location mLastLocation;


    private Button mLogout , mRequest , mSettings , mCancle , mHistory ;
    private  LatLng customerLocation;
    private SupportMapFragment mapFragment;
    private Boolean requestBol = false ;
    private Marker HomeMarker , mMaidMarker ;;
    private LinearLayout mMaidInfo;
    private TextView mMaidName , mMaidPhone ;
    private double mMonyeShouldPay ;
    private int mHowLong  ;
    private RatingBar mRatingBar;
    boolean toolsNeeded;


    @Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_map);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
         mapFragment = (SupportMapFragment) getSupportFragmentManager()
        .findFragmentById(R.id.map);

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(CustomerMapActivity.this , new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }
    else {
        mapFragment.getMapAsync(this);
    }

        mMaidInfo = (LinearLayout) findViewById(R.id.maidInfo);
        mMaidName = (TextView) findViewById(R.id.maidName);
        mMaidPhone = (TextView) findViewById(R.id.MaidPhone);

        mLogout = (Button) findViewById(R.id.logout);
        mRequest = (Button) findViewById(R.id.request) ;
        mSettings = (Button) findViewById(R.id.settings);
        mCancle = (Button) findViewById(R.id.cancle);
        mHistory = (Button) findViewById(R.id.history);

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);


        mCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                   endWork();

            }
        });
        mLogout.setOnClickListener(new View.OnClickListener() {

            @Override
        public void onClick(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(CustomerMapActivity.this , MainActivity.class);
        startActivity(intent);
        finish();
        return;
        }
        });

    mRequest.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CustomerMapActivity.this , HoursActivity.class) ;
            startActivityForResult(intent, 1);
        }
    });

    mSettings.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CustomerMapActivity.this , CustomerSettingsActivity.class);
            startActivity(intent);
            return;
        }
    });

        mHistory.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent(CustomerMapActivity.this , HistoryActivity.class);
            intent.putExtra("customerOrMaid" , "Customers");
            startActivity(intent);
            return;
        }
    });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        // check that it is the SecondActivity with an OK result
         if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String HL = data.getStringExtra("time").toString() ;
                mHowLong= Integer.parseInt(HL );
                String TN = data.getStringExtra("tool").toString();
                toolsNeeded =Boolean.parseBoolean(TN);
                requestBol =true ;
                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                GeoFire geoFire = new GeoFire(ref);
                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                customerLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                HomeMarker= mMap.addMarker(new MarkerOptions().position(customerLocation).title("منزل الزبون هنا !"));
                mRequest.setText("نبحث عن اقرب خادمه لكِ ...");
                mCancle.setVisibility(View.VISIBLE);
                getClosestMaid();
            }
        }

    }
    private  int radius = 1 ;
    private  Boolean maidFound = false ;
    private String maidFoundID ;

    GeoQuery geoQuery;

    private void getClosestMaid() {
    DatabaseReference MaidLocation = FirebaseDatabase.getInstance().getReference().child("maidsAvailable");

    GeoFire geoFire = new GeoFire(MaidLocation);
           geoQuery = geoFire.queryAtLocation(new GeoLocation(customerLocation.latitude , customerLocation.longitude) , radius);
           geoQuery.removeAllListeners();
           geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!maidFound && requestBol ) {

                                maidFound = true;
                                maidFoundID = key;
                                DatabaseReference maidRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidFoundID).child("customerRequest");
                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap map = new HashMap();

                                map.put("timeNeeded" ,mHowLong) ;
                                map.put("toolsNeeded" , toolsNeeded) ;
                                map.put("customerHomeId", customerId);
                                maidRef.updateChildren(map);

                                getMaidLocation();
                                getMaidInfo();
                                getHasWorkEnded();
                                mRequest.setText("نبحث عن موقع الخادمه ...");
                            }

                }
            @Override
            public void onKeyExited(String key) { }
            @Override
            public void onKeyMoved(String key, GeoLocation location) {  }
            @Override
            public void onGeoQueryReady() {
                if (!maidFound)
                {
                    radius++;
                    getClosestMaid();
                }
            }
            @Override
            public void onGeoQueryError(DatabaseError error) { }
});
        }

    private DatabaseReference maidLocationRef;
    private ValueEventListener maidLocationRefListener;

    private void getMaidLocation() {
        maidLocationRef = FirebaseDatabase.getInstance().getReference().child("maidsWorking").child(maidFoundID).child("l");
        maidLocationRefListener= maidLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0 ;
                    double locationLng = 0 ;
                    mRequest.setText("وجدنا خادمه لكِ");
                    if (map.get(0) != null ){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null ){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                    LatLng maidLatLng = new LatLng(locationLat ,locationLng) ;
                    if (mMaidMarker!= null ){
                        mMaidMarker.remove();
                    }
                    Location loc1 = new Location("");
                    loc1.setLatitude(customerLocation.latitude);
                    loc1.setLongitude(customerLocation.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(maidLatLng.latitude);
                    loc2.setLongitude(maidLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);
                    if (distance <50){
                        mRequest.setText("خادمتكِ هنا !");
                    }
                    if(distance<200)
                        mCancle.setEnabled(false);
                    else {
                        mRequest.setText("وجدت الخادمه على بعد" + String.valueOf(distance));
                    }

                    mMaidMarker= mMap.addMarker(new MarkerOptions().position(maidLatLng).title("خادمتك").icon(BitmapDescriptorFactory.fromResource(R.mipmap.maidicon)));

                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private void getMaidInfo(){
        mMaidInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabaes = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidFoundID);
        mCustomerDatabaes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mMaidName.setText(dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mMaidPhone.setText(dataSnapshot.child("phone").getValue().toString());
                    }

                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }
    private DatabaseReference workHasEndedRef ;
    private ValueEventListener workHasEndedRefListener;
    private void getHasWorkEnded(){
        workHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidFoundID).child("customerRequest");
        workHasEndedRefListener =workHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }
                else{

                    endWork();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void endWork () {
        requestBol = false ;
        geoQuery.removeAllListeners();
        maidLocationRef.removeEventListener(maidLocationRefListener);
        workHasEndedRef.removeEventListener(workHasEndedRefListener);

        if (maidFoundID!=null){
            DatabaseReference maidRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidFoundID).child("customerRequest");
            maidRef.removeValue();
            maidFoundID = null;
        }
        maidFound=false ;
        radius=1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);

      if (mMaidMarker != null){
            mMaidMarker.remove();
        }

        mRequest.setText("اطلب خادمه");
        mCancle.setVisibility(View.GONE);
        mMaidInfo.setVisibility(View.GONE);
        mMaidName.setText("");
        mMaidPhone.setText("");

    }

@Override
public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(CustomerMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
        }

protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
        mGoogleApiClient.connect();
        }
@Override
public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
 }
@Override
public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000); //1000 MS
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        ActivityCompat.requestPermissions(CustomerMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
    }
    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

}

@Override
public void onConnectionSuspended(int i) {

        }

@Override
public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }
    final int LOCATION_REQUEST_CODE = 1;
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case LOCATION_REQUEST_CODE:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mapFragment.getMapAsync(this);
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }
@Override
protected void onStop(){
        super.onStop();
  }
}
