package edu.nyu.cs9233.callnotes.home;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.calls.CallEntity;
import edu.nyu.cs9233.callnotes.profile.UserDetailsManager;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class CallSummaryActivity extends AppCompatActivity {

    private TextView nameTextView;
    private TextView emailTextView;
    private TextView timeTextView;
    private TextView callSummaryTextView;
    private TextView callTranscriptionTextView;

    private CurrentCognitoUser currentCognitoUser;
    private CallEntity callEntity;
    private CallUser callUser;
    private CallHistory callHistory;

    private static final String LOADING = "loading";

    public static void start(Context context, CallEntity callEntity, CallUser callUser) {
        Intent intent = new Intent(context, CallSummaryActivity.class);
        intent.putExtra("CallEntity", callEntity);
        intent.putExtra("CallUser", callUser);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_summary);

        nameTextView = findViewById(R.id.nameText);
        emailTextView = findViewById(R.id.emailText);
        timeTextView = findViewById(R.id.timeText);
        callSummaryTextView = findViewById(R.id.callSummaryText);
        callTranscriptionTextView = findViewById(R.id.callTranscriptionText);

        this.callEntity = (CallEntity) getIntent().getSerializableExtra("CallEntity");
        this.callUser = (CallUser) getIntent().getSerializableExtra("CallUser");

        loadUi();

        CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
            @Override
            public void onSuccess(CurrentCognitoUser object) {
                CallSummaryActivity.this.currentCognitoUser = object;
                UserDetailsManager userDetailsManager = new UserDetailsManager(CallSummaryActivity.this, object);
                fetchCalHistory(userDetailsManager);
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(CallSummaryActivity.this, e.getMessage());
            }
        });
    }

    public void fetchCalHistory(UserDetailsManager userDetailsManager) {
        Debug.log("FETCHING HISTORY FOR CALL ID: ", callEntity.getCallId());
        userDetailsManager.fetchCallNotes(callEntity.getCallId(), new Listeners.GetObjectListener<CallHistory>() {
            @Override
            public void onSuccess(CallHistory callHistory) {
                Debug.log("CALL HISTORY: ", callHistory);
                CallSummaryActivity.this.callHistory = callHistory;
                loadUi();
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(CallSummaryActivity.this, e.getMessage());
            }
        });
    }

    private void loadUi() {
        if (currentCognitoUser == null) {
            Log.v("CallSummaryActivity", "Current Cognito User is null");
            return;
        }
        if (callEntity != null && callUser != null) {
            emailTextView.setText(callUser.getEmail());
            nameTextView.setText(callUser.getName());
            timeTextView.setText(Utils.getAgoTimestamp(callEntity.getCreatedAt().toDate()));
        } else {
            emailTextView.setText(LOADING);
            nameTextView.setText(LOADING);
            timeTextView.setText(LOADING);
        }

        if (callHistory != null) {
            callSummaryTextView.setText(callHistory.getCallSummmary());
            callTranscriptionTextView.setText(callHistory.getCallTranscription());
        } else {
            callSummaryTextView.setText("No summary exists for this call");
            callTranscriptionTextView.setText("No transcription exists for this call");
        }
    }

}
