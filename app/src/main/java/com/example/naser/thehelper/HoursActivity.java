package com.example.naser.thehelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HoursActivity extends AppCompatActivity {


  private Switch mSwitch ;
  private Button mNext ;
  private SeekBar mSeekBar ;
  private TextView mPay , mPayF;
  double mMonye ;
  boolean toolsNeeded  ;
  int howLong =5;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hours);
        mSwitch = (Switch) findViewById(R.id.switch1) ;
        mSeekBar = (SeekBar) findViewById(R.id.seekBar ) ;
        mNext = (Button) findViewById(R.id.next) ;
        mPay = (TextView) findViewById(R.id.pay) ;
        mPayF = (TextView) findViewById(R.id.shouldpay) ;

        mPay.setText(5+ " ساعات " + "(5JD/ساعه)");
        mPayF.setText(5* 5 + "JD");

        mSeekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser)
            {

                int x = mSeekBar.getProgress() + 2;
                howLong = x;

                mPay.setText(x+ " ساعات " + "(5JD/ساعه)");
                if(mSwitch.isChecked()) {
                    mPayF.setText(x * 5 + 6 + "JD");
                    mMonye = x*5 + 6 ;
                    toolsNeeded = true ;
                }else{
                    mPayF.setText(x * 5 + "JD");
                    mMonye = x*5  ;
                    toolsNeeded = false ;
                }

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                int x = mSeekBar.getProgress()+2 ;
                howLong =x;

                if(mSwitch.isChecked()) {
                    mPayF.setText(x * 5 + 6 + "JD");
                    mMonye = x*5 + 6 ;
                    toolsNeeded = true;

                }else{

                    mPayF.setText(x * 5 + "JD");
                    mMonye = x*5  ;
                    toolsNeeded= false ;
                }
            }
        });
        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
              //  intent.putExtra("monye", mMonye);
                intent.putExtra("time" , howLong+"");
                intent.putExtra("tool" , toolsNeeded+"") ;
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }
}
