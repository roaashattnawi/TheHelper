package com.example.naser.thehelper;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.naser.thehelper.historyRecyclerView.HistoryAdapter;
import com.example.naser.thehelper.historyRecyclerView.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.*;
import java.util.Calendar ;
import java.lang.String ;


public class HistoryActivity extends AppCompatActivity {
    private String customerOrMaid, userId;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView) ;
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);

        mHistoryLayoutManager = new LinearLayoutManager(HistoryActivity.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);

        mHistoryAdapter=new HistoryAdapter(getDataSetHistory() , HistoryActivity.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        customerOrMaid = getIntent().getExtras().getString("customerOrMaid");
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();



    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(customerOrMaid).child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history: dataSnapshot.getChildren()){
                        FetchWorkInfomation(history.getKey()) ;

                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void FetchWorkInfomation(String homeKey) {

        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(homeKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String homeId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    for(DataSnapshot child :dataSnapshot.getChildren()){
                        if(child.getKey().equals("timestamp")){
                            timestamp=Long.valueOf(child.getValue().toString());

                        }
                    }
                    HistoryObject obj = new HistoryObject(homeId,getDate(timestamp)) ;
                    resultHistory.add(obj);
                    mHistoryAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });}
    private String getDate(Long timestamp) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(timestamp*1000);

        android.text.format.DateFormat df = new android.text.format.DateFormat();
        String date = df.format("dd-MM-yyyy hh:mm",cal).toString();
        return date ;
    }


    private ArrayList resultHistory = new ArrayList<HistoryObject>();
  private ArrayList<HistoryObject> getDataSetHistory () {
      return resultHistory;

  }
}
