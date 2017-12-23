package com.example.naser.thehelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.*;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistorySingleActivity extends AppCompatActivity implements OnMapReadyCallback , RoutingListener {

    private String workId ,currentUserId, customerId, maidId, userMaidOrCustomer;

    private GoogleMap mMap;
    private SupportMapFragment mMapFragment;
    private TextView homeLocation;
    private TextView workTime , workTools;
    private TextView workDate;
    private TextView userName;
    private TextView userPhone;
    private RatingBar mRatingBar;
    private LatLng maidLocationLatLng, homeLatLng;
    private Double workPrice = 0.0;
    private boolean toolsNeeded =false  ;
    private DatabaseReference historyWorkInfoDb;
    private Button mPay;
    private Boolean customerPaid = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_single);


        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        polylines = new ArrayList<>();
        workId = getIntent().getExtras().getString("homeId");
        mMapFragment= (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map);
        mMapFragment.getMapAsync(this);

        homeLocation = (TextView) findViewById(R.id.workLoaction);
        workTime = (TextView) findViewById(R.id.workTime);
        workDate = (TextView) findViewById(R.id.workDate);
        userName = (TextView) findViewById(R.id.userName);
        userPhone = (TextView) findViewById(R.id.userPhone);
        workTools = (TextView) findViewById(R.id.workTools);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mPay = findViewById(R.id.pay);

        mPay.setVisibility(View.VISIBLE);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        historyWorkInfoDb = FirebaseDatabase.getInstance().getReference().child("history").child(workId);
        getRideInformation();
    }

    private void getRideInformation() {
        historyWorkInfoDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        if (child.getKey().equals("customer")) {
                            customerId = child.getValue().toString();
                            if (!customerId.equals(currentUserId)) {
                                userMaidOrCustomer = "Maids";
                                getUserInformation("Customers", customerId);

                            }
                        }
                        if (child.getKey().equals("maid")) {
                            maidId = child.getValue().toString();
                            if (!maidId.equals(currentUserId)) {
                                userMaidOrCustomer = "Customers";
                                getUserInformation("Maids", maidId);
                                displayCustomerRelatedObjects() ;
                            }
                        }
                        if (child.getKey().equals("timestamp")){
                            workDate.setText(getDate(Long.valueOf(child.getValue().toString())));
                        }
                        if (child.getKey().equals("timeNeeded")){
                            workTime.setText("عدد الساعات التي عملت الخادمه لديك : " + child.getValue().toString() + "ساعات");
                            workPrice= Double.valueOf(child.getValue().toString()) * 5 ;
                        }
                        if (child.getKey().equals("toolsNeeded")){
                            workTools.setText("هل احتاج الزبون ادوات تنظيف ؟ " + child.getValue().toString() );
                            toolsNeeded = Boolean.parseBoolean(child.getValue().toString());
                            if (toolsNeeded)
                            workPrice+=6 ;
                            else
                                workPrice = workPrice ;
                        }
                        if (child.getKey().equals("rating")){
                            mRatingBar.setRating(Integer.valueOf(child.getValue().toString()));

                        }
                        if (child.getKey().equals("location")){
                            homeLocation.setText("");
                            homeLatLng = new LatLng(Double.valueOf(child.child("from").child("lat").getValue().toString()), Double.valueOf(child.child("from").child("lng").getValue().toString()));
                            maidLocationLatLng = new LatLng(Double.valueOf(child.child("to").child("lat").getValue().toString()), Double.valueOf(child.child("to").child("lng").getValue().toString()));
                            if(maidLocationLatLng != new LatLng(0,0)){
                               getRouteToMarker();
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void displayCustomerRelatedObjects() {
        mRatingBar.setVisibility(View.VISIBLE);
        mPay.setVisibility(View.VISIBLE);
        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                historyWorkInfoDb.child("rating").setValue(rating);
                DatabaseReference mDriverRatingDb = FirebaseDatabase.getInstance().getReference().child("Users").child("Maids").child(maidId).child("rating");
                mDriverRatingDb.child(workId).setValue(rating);
            }
        });

       /* if(customerPaid){
            mPay.setEnabled(false);
        }else{
            mPay.setEnabled(true);
        }*/
        mPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                payPalPayment();
            }
        });

    }
    private int PAYPAL_REQUEST_CODE = 1;
    private static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
           .clientId(PaypalConfig.PAYPAL_CLIENT_ID);

    private void payPalPayment() {
        PayPalPayment payment = new PayPalPayment(new BigDecimal(workPrice), "USD", "Uber Ride",
                PayPalPayment.PAYMENT_INTENT_SALE);

        Intent intent = new Intent(this, PaymentActivity.class);

        // intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){

            }else{
                Toast.makeText(getApplicationContext(), "Payment unsuccessful", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }



    private void getUserInformation(String otherUserDriverOrCustomer, String otherUserId) {
        DatabaseReference mOtherUserDB = FirebaseDatabase.getInstance().getReference().child("Users").child(otherUserDriverOrCustomer).child(otherUserId);
        mOtherUserDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name") != null){
                        userName.setText(map.get("name").toString());
                    }
                    if(map.get("phone") != null){
                        userPhone.setText(map.get("phone").toString());
                    }
                }

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String date = df.format("dd-MM-yyyy hh:mm",cal).toString();
        return date ;
    }


    private void getRouteToMarker() {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(maidLocationLatLng, homeLatLng)
                .build();
        routing.execute();
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap= googleMap;
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
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(homeLatLng);
        builder.include(maidLocationLatLng);
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int padding = (int) (width*0.2);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding);

        mMap.animateCamera(cameraUpdate);

        mMap.addMarker(new MarkerOptions().position(homeLatLng).title("home location"));
        mMap.addMarker(new MarkerOptions().position(maidLocationLatLng).title("maid location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.maidicon)));

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
