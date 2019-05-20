package edu.nyu.cs9233.callnotes.authentication;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.home.HomeActivity;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class PreAuthActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_auth);

        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.signUpUrl)));
                startActivity(browserIntent);
            }
        });

        findViewById(R.id.signInButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PreAuthActivity.this, AuthenticationActivity.class));
                finish();
            }
        });

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        Intent i = new Intent(PreAuthActivity.this, AuthenticationActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case SIGNED_OUT:
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Utils.showErrorAlert(PreAuthActivity.this, e.getMessage());
            }
        });
    }
}
