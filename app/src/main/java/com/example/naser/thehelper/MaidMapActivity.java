package com.example.naser.thehelper;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MaidMapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener  , RoutingListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    Location mLastLocation;

    private  LatLng maidLocation , homeLatLng;
    private SupportMapFragment mapFragment;

    private Button mLogout , mSettings , mWorkStatus ,mDoneWork , mMaidHasArrived;
    private Switch mWorkingSwitch;
    private String customerId = "";
    private Boolean isLogginOut = false ;

    private int status = 0 ;
    private LinearLayout mCustomerInfo; ;
    private TextView mCustomerName , mCustomerPhone , mCustomerTimeNeeded ,mCustomerToolsNeeded ;
    public  int counter = 0 ;
    Location loc2 , loc1;
    String timeNeeded  , toolsNeeded;
    private Date currentDate = new Date();
    float distance =0;
    private double timeWhenArrived , timeWhenFinish , timeWorked=0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maid_map);
        polylines = new ArrayList<>();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MaidMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        else {
            mapFragment.getMapAsync(this);
        }


        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);
        mCustomerName = (TextView) findViewById(R.id.CustomerName);
        mCustomerPhone = (TextView) findViewById(R.id.CustomerPhone);
        mCustomerTimeNeeded = (TextView) findViewById(R.id.CustomerTimeNeeded);
        mCustomerToolsNeeded = (TextView) findViewById(R.id.CustomerToolNeeded);
        mSettings=(Button) findViewById(R.id.settings);
        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MaidMapActivity.this , MaidSettingActivity.class);
                startActivity(intent);
                //finish();
                return;


            }
        });
        mLogout = (Button) findViewById(R.id.logout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLogginOut = true;
                discounnectMaid();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MaidMapActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
                        });

        mWorkingSwitch = (Switch) findViewById(R.id.workingSwitch) ;
        mWorkingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked){
                    connectMaid ();
                }else {
                    discounnectMaid();
                }

            }
        });
        getAssignedCustomer();
        mWorkStatus=(Button) findViewById(R.id.workStatus);
        mWorkStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                        StartCountTime();

            }
        });
        mDoneWork = (Button) findViewById(R.id.doneWork);
        mDoneWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FinishCountTime();
            }
        });

    }

    private void getAssignedCustomer(){
        String maidId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidId).child("customerRequest").child("customerHomeId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    customerId = dataSnapshot.getValue().toString();
                    getAssignedCustomerHomeLocation();
                    getAssignedCustomerTimeNeeded();
                    getAssignedCustomerToolsNeeded();
                    getAssignedCustomerInfo();
                    status = 1 ;

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

    private void getAssignedCustomerToolsNeeded() {
        String maidId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("timeNeeded")!=null){
                        timeNeeded = map.get("timeNeeded").toString();

                        mCustomerTimeNeeded.setText("عدد الساعات التي يحتاجها الزبون: " + timeNeeded);
                    }

                    else {
                        mCustomerTimeNeeded.setText("عدد الساعات التي يحتاجها الزبون: --");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getAssignedCustomerTimeNeeded(){
        String maidId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference assignedCustomerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidId).child("customerRequest");
        assignedCustomerRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("toolsNeeded")!=null){
                         toolsNeeded = map.get("toolsNeeded").toString();

                      mCustomerToolsNeeded.setText("هل يحتاج الزبون ادوات تنظيف؟ " + toolsNeeded);
                    }

                else {
                       mCustomerToolsNeeded.setText("هل يحتاج الزبون ادوات تنظيف؟ --");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    private void getAssignedCustomerInfo(){
        mCustomerInfo.setVisibility(View.VISIBLE);
       DatabaseReference mCustomerDatabaes = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId);
        mCustomerDatabaes.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String , Object> map = (Map<String,Object>)dataSnapshot.getValue();
                    if (map.get("name") != null){
                        mCustomerName.setText(map.get("name").toString());
                    }
                    if (map.get("phone") != null) {
                        mCustomerPhone.setText(map.get("phone").toString());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

            Marker homeMarker ;
    private  DatabaseReference assignedCustomerHomeLocationRef;
    private ValueEventListener assignedCustomerHomeLocationRefListener;

    private void getAssignedCustomerHomeLocation(){

        assignedCustomerHomeLocationRef = FirebaseDatabase.getInstance().getReference().child("customerRequest").child(customerId).child("l");
        assignedCustomerHomeLocationRefListener= assignedCustomerHomeLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locationLat = 0 ;
                    double locationLng = 0 ;
                    if (map.get(0) != null ){
                        locationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1) != null ){
                        locationLng=Double.parseDouble(map.get(1).toString());
                    }
                     homeLatLng = new LatLng(locationLat ,locationLng) ;

                    maidLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());


                    homeMarker=mMap.addMarker(new MarkerOptions().position(homeLatLng).title("هنا منزل الزبون"));
                    getRouteToMarker(homeLatLng);

                        loc2 = new Location("");
                        loc2.setLatitude(homeLatLng.latitude);
                        loc2.setLongitude(homeLatLng.longitude);

                    loc1 = new Location("");
                    loc1.setLatitude(mLastLocation.getLatitude());
                    loc1.setLongitude(mLastLocation.getLongitude());

                    distance = loc1.distanceTo(loc2);



                }
            }
             @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    private void StartCountTime() {

        SimpleDateFormat simpleDate2 = new SimpleDateFormat("HH");
        String hours12 = simpleDate2.format(currentDate);

    /*for minutes*/
        SimpleDateFormat simpleDate3 = new SimpleDateFormat("mm");
        String minutes = simpleDate3.format(currentDate);
        //double min = Double.parseDouble(minutes)/100;
        timeWhenArrived = (Double.parseDouble(hours12+minutes))/100;
        erasePolylines();
        mWorkStatus.setVisibility(View.GONE);
        mDoneWork.setVisibility(View.VISIBLE);

    }
    private void FinishCountTime() {
        SimpleDateFormat simpleDate2 = new SimpleDateFormat("HH");
        String hours12 = simpleDate2.format(currentDate);

    /*for minutes*/
        SimpleDateFormat simpleDate3 = new SimpleDateFormat("mm");
        String minutes = simpleDate3.format(currentDate);
        //double min = Double.parseDouble(minutes)/100;
        timeWhenFinish = timeWhenArrived+Double.parseDouble(timeNeeded);

        timeWorked = timeWhenFinish - timeWhenArrived ;
        recordWork();
        endWork();


    }

    private void getRouteToMarker(LatLng homeLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude() , mLastLocation.getLongitude()), homeLatLng)
                .build();
        routing.execute();
    }

    private void endWork () {
        erasePolylines();
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference maidRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(userId).child("customerRequest");
        maidRef.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(customerId);
        customerId = "" ;

        if (homeMarker != null) {
            homeMarker.remove();
        }
        if(assignedCustomerHomeLocationRefListener != null) {
            assignedCustomerHomeLocationRef.removeEventListener(assignedCustomerHomeLocationRefListener);
        }

        mCustomerInfo.setVisibility(View.GONE);
        mCustomerName.setText("");
        mCustomerPhone.setText("");
        mCustomerTimeNeeded.setText("عدد الساعات التي يحتاجها الزبون: -- " );
        mCustomerToolsNeeded.setText("هل يحتاج الزبون ادوات تنظيف ؟ --");

    }

    private void recordWork(){

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference customerRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(customerId).child("history");
        DatabaseReference maidRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(userId).child("history");
        DatabaseReference historyRef = FirebaseDatabase.getInstance().getReference().child("history");
        String requestId = historyRef.push().getKey();
        maidRef.child(requestId).setValue(true);
        customerRef.child(requestId).setValue(true);

        HashMap map = new HashMap();
        map.put("maid" , userId) ;
        map.put("customer" , customerId);
        map.put("rating" , 0) ;
        map.put("timestamp" , getCurrentTimestamp()) ;
        map.put("timeNeeded" , timeWorked) ;
        map.put("location/from/lat" , maidLocation.latitude) ;
        map.put("location/from/lng" , maidLocation.longitude) ;
        map.put("location/to/lat" , homeLatLng.latitude) ;
        map.put("location/to/lng" , homeLatLng.longitude) ;
        map.put("toolsNeeded" , toolsNeeded) ;



        historyRef.child(requestId).updateChildren(map);
    }

    private Long getCurrentTimestamp() {
        Long timestamp = System.currentTimeMillis()/1000;
        return timestamp;

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
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
        if (getApplicationContext()!= null) {
            mLastLocation = location;
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            mMap.animateCamera(CameraUpdateFactory.zoomTo(15));


            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("maidsAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("maidsWorking");
            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking);

            switch (customerId){
                case "":
                    geoFireWorking.removeLocation(userId);
                    geoFireAvailable.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));
                    break;
                default:
                    geoFireAvailable.removeLocation(userId);
                    geoFireWorking.setLocation(userId, new GeoLocation(location.getLatitude(), location.getLongitude()));

                    break;
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    private void connectMaid () {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MaidMapActivity.this , new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);

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

    private void discounnectMaid() {


        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("maidsAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId);
    }


    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};

    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative Fcusroutes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {
    }
    private void erasePolylines(){
        for (Polyline line : polylines){
            line.remove();

        }
        polylines.clear();
    }
}
