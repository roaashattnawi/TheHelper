package com.example.naser.thehelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class CustomerSignupActivtiy extends AppCompatActivity {

    private Button signup;
    private TextView mEmail, mPassword, mPasswordConfim, mFullname, mPhone, error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_signup_activtiy);
        signup = (Button) findViewById(R.id.signup);
        mEmail = (TextView) findViewById(R.id.email);
        mPassword = (TextView) findViewById(R.id.password);
        mPasswordConfim = (TextView) findViewById(R.id.pass2);
        mFullname = (TextView) findViewById(R.id.fullname);
        mPhone = (TextView) findViewById(R.id.phone);
        error = (TextView) findViewById(R.id.error);

        if (!mPassword.getText().toString().equals(mPasswordConfim.getText().toString()))
            error.setVisibility(View.VISIBLE);

        else {
            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.putExtra("email", mEmail.getText().toString());
                    intent.putExtra("password", mPassword.getText().toString());
                    intent.putExtra("fullname", mFullname.getText().toString());
                    intent.putExtra("phone", mPhone.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();

                }
            });
        }
    }

}
