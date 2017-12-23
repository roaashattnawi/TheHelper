package com.example.naser.thehelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MaidOrCustomerActivtiy extends AppCompatActivity {
    private Button Maid , Customer ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maid_or_customer_activtiy);
        Maid = (Button) findViewById(R.id.Maids) ;
        Customer =(Button) findViewById(R.id.Customer);
        startService(new Intent(MaidOrCustomerActivtiy.this , OnAppKilled.class)) ;
        Maid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MaidOrCustomerActivtiy.this, MaidLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        Customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MaidOrCustomerActivtiy.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
    }
}
