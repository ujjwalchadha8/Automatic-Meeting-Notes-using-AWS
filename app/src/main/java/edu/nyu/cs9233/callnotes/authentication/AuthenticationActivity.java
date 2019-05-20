package edu.nyu.cs9233.callnotes.authentication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignInUIOptions;
import com.amazonaws.mobile.client.UserStateDetails;

import edu.nyu.cs9233.callnotes.MainActivity;
import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.home.HomeActivity;
import edu.nyu.cs9233.callnotes.utils.ClientFactory;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class AuthenticationActivity extends AppCompatActivity {

    private static final String TAG = AuthenticationActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication_activity);

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, userStateDetails.getUserState().toString());

                switch (userStateDetails.getUserState()){
                    case SIGNED_IN:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(AuthenticationActivity.this, "SUCCESSFUL SIGN IN", Toast.LENGTH_SHORT).show();
                            }
                        });
                        CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
                            @Override
                            public void onSuccess(CurrentCognitoUser cognitoUser) {
                                Debug.log("SIGN-IN: " + cognitoUser);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                throw new RuntimeException(e);
                            }
                        });

                        Intent i = new Intent(AuthenticationActivity.this, HomeActivity.class);
                        startActivity(i);
                        finish();
                        break;
                    case SIGNED_OUT:
                        showSignIn();
                        break;
                    default:
                        AWSMobileClient.getInstance().signOut();
                        showSignIn();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, e.toString());
            }
        });

    }

    private void showSignIn() {
        try {
            AWSMobileClient.getInstance().showSignIn(this,
                    SignInUIOptions.builder()
                            .nextActivity(HomeActivity.class).build());
        } catch (Exception e) {
            Utils.showErrorAlert(AuthenticationActivity.this, e.getMessage());
        }
    }
}
