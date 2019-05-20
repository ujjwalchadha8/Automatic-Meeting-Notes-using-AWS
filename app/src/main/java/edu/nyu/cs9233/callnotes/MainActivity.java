package edu.nyu.cs9233.callnotes;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.danlew.android.joda.JodaTimeAndroid;

import edu.nyu.cs9233.callnotes.authentication.AuthenticationActivity;
import edu.nyu.cs9233.callnotes.authentication.PreAuthActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        JodaTimeAndroid.init(this);
        startActivity(new Intent(this, PreAuthActivity.class));
        finish();
    }

}
