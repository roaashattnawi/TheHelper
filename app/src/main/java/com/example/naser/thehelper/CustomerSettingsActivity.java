package com.example.naser.thehelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CustomerSettingsActivity extends AppCompatActivity {

private EditText mNameField , mPhoneFiled ;
private Button mBack , mConfirm;
private FirebaseAuth mAuth;
private DatabaseReference mCustomerDatabaes;
private String userID;
private String mName ;
private String mPhone;


@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);
        mNameField = (EditText)findViewById(R.id.name);
        mPhoneFiled = (EditText)findViewById(R.id.phone);

        mBack = (Button) findViewById(R.id.back);
        mConfirm = (Button) findViewById(R.id.confirm);

        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid() ;
        mCustomerDatabaes = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);
        getUesrInfo();
        mConfirm.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        saveUserInfo();
        }
        });

        mBack.setOnClickListener(new View.OnClickListener() {
@Override
public void onClick(View view) {
        finish();
        return;
        }
        });
        }
private void getUesrInfo(){
        mCustomerDatabaes.addValueEventListener(new ValueEventListener() {
@Override
public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
        Map<String , Object> map = (Map<String,Object>)dataSnapshot.getValue();
        if (map.get("name") != null){
        mName = map.get("name").toString();
        mNameField.setText(mName);
        }
        if (map.get("phone") != null){
        mPhone = map.get("phone").toString();
        mPhoneFiled.setText(mPhone);
        }

        }

        }

@Override
public void onCancelled(DatabaseError databaseError) {

        }
        });
        }

private void saveUserInfo() {
        mName=mNameField.getText().toString();
        mPhone = mPhoneFiled.getText().toString();
        Map userInfo = new HashMap();
        userInfo.put("name" , mName);
        userInfo.put("phone" , mPhone) ;
        mCustomerDatabaes.updateChildren(userInfo) ;
        Toast.makeText(getApplicationContext(), "Information Updated", Toast.LENGTH_LONG).show();
        finish();
        }
        }
