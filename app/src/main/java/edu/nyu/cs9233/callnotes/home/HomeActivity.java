package edu.nyu.cs9233.callnotes.home;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.authentication.PreAuthActivity;
import edu.nyu.cs9233.callnotes.calls.CallActivity;
import edu.nyu.cs9233.callnotes.calls.CallEntity;
import edu.nyu.cs9233.callnotes.calls.CallItemDBManager;
import edu.nyu.cs9233.callnotes.calls.CallPresenter;
import edu.nyu.cs9233.callnotes.calls.CallSearchManager;
import edu.nyu.cs9233.callnotes.callsearch.CallSearchActivity;
import edu.nyu.cs9233.callnotes.profile.UserDetailsManager;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private AutoCompleteTextView callEditReceiverText;
    private View callButton;
    private ArrayAdapter<String> userSuggestionsAdapter;
    private ArrayList<CallUser> callableUsers;
    private ListView callHistoryListView;
    private CallHistoryAdapter callHistoryAdapter;
    private View searchFAB;
    private List<CallEntity> callHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        getRequiredPermissionsFromUser();
        initUI();
        CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
            @Override
            public void onSuccess(CurrentCognitoUser user) {
                callHistoryAdapter = new CallHistoryAdapter(HomeActivity.this, user.getSub(), new ArrayList<CallEntity>(), new ArrayList<CallUser>());
                final UserDetailsManager userDetailsManager = new UserDetailsManager(HomeActivity.this, user);
                saveUserDetailsToDBIfAbsent(userDetailsManager);
                fetchCallableUsers(userDetailsManager);
                listenForIncomingCalls(userDetailsManager);
                callHistoryListView.setAdapter(callHistoryAdapter);
                new CallItemDBManager(HomeActivity.this).fetchCallHistory(user.getSub(), new Listeners.GetObjectListener<List<CallEntity>>() {
                    @Override
                    public void onSuccess(List<CallEntity> callEntities) {
                        List<CallEntity> sortedCalls = CallEntity.sortByDescendingTimestamp(callEntities);
                        callHistoryAdapter.addAll(sortedCalls);
                        HomeActivity.this.callHistory = sortedCalls;
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Utils.showErrorAlert(HomeActivity.this, e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(HomeActivity.this, e.getMessage());
            }
        });

    }

    private void fetchCallableUsers(final UserDetailsManager userDetailsManager) {
        userDetailsManager.getAllUsers(new Listeners.GetObjectListener<List<CallUser>>() {
            @Override
            public void onSuccess(List<CallUser> users) {
                Debug.log("FETCHED USER LIST: ", users);
                callableUsers = new ArrayList<>();
                for (CallUser user : users) {
                    if (!userDetailsManager.getCurrentCognitoUser().getEmail().equals(user.getEmail())) {
                        userSuggestionsAdapter.add(user.getEmail());
                        callHistoryAdapter.addAllCallUsers(users);
                        callableUsers.add(user);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(HomeActivity.this, e.getMessage());
            }
        });
    }

    private void getRequiredPermissionsFromUser() {
        Utils.isPermissionGranted(Manifest.permission.RECORD_AUDIO, this);
    }

    private void initUI() {
        callEditReceiverText = findViewById(R.id.callReceiverIdEditText);
        callEditReceiverText.setThreshold(1);
        userSuggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line);
        callEditReceiverText.setAdapter(userSuggestionsAdapter);
        callButton = findViewById(R.id.callButton);
        callButton.setOnClickListener(this);
        callHistoryListView = findViewById(R.id.callHistoryListView);
        searchFAB = findViewById(R.id.searchCallsFAB);
        searchFAB.setOnClickListener(this);
    }

    private void saveUserDetailsToDBIfAbsent(final UserDetailsManager userDetailsManager) {
        userDetailsManager.getUser(new Listeners.GetObjectListener<CallUser>() {
            @Override
            public void onSuccess(CallUser user) {
                Debug.log("CALL USER: ", user);
                if (user == null) {
                    userDetailsManager.createUserDetails(new Listeners.PersistObjectListener() {
                        @Override public void onSuccess() {}

                        @Override
                        public void onFailure(Exception e) {
                            Utils.showErrorAlert(HomeActivity.this, e.getMessage());
                        }
                    });
                    new AlertDialog.Builder(HomeActivity.this)
                            .setTitle("Email Notifications")
                            .setMessage("To receive email notifications, you must click on the verification email link we just sent you")
                            .setPositiveButton("OK", null)
                            .show();
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("Error fetching user", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void listenForIncomingCalls(final UserDetailsManager userDetailsManager) {
        CallItemDBManager callItemDBManager = new CallItemDBManager(this);
        callItemDBManager.listenToIncomingCalls(new Listeners.ChangeListener<CallEntity>() {
            @Override
            public void onChange(final CallEntity entity) {
                final HashMap<String, String> callData = new HashMap<>();
                userDetailsManager.getUser(entity.getDialerId(), new Listeners.GetObjectListener<CallUser>() {
                    @Override
                    public void onSuccess(CallUser user) {
                        callData.put("name", user.getName());
                        CallActivity.createIncomingCall(HomeActivity.this, entity.getCallId(), callData);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Utils.showErrorAlert(HomeActivity.this, e.getMessage());
                    }
                });

            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }

            @Override
            public void onCompleted(CallEntity entity) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.home_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logoutOptionsMenu:
                AWSMobileClient.getInstance().signOut();
                finish();
                startActivity(new Intent(HomeActivity.this, PreAuthActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.callButton:
                String text = callEditReceiverText.getText().toString().replaceAll("\\s", "");
                if (getCallableEmails() == null) {
                    Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
                    return;
                }
                String callUserId = getCallIdForEmail(text);
                if (callUserId == null) {
                    Toast.makeText(this, "Please enter a valid user id.", Toast.LENGTH_SHORT).show();
                    return;
                }
                getTriggersAndCall(callUserId);
                break;
            case R.id.searchCallsFAB:
                 if (callHistory != null) {
                     CallSearchActivity.start(this, callHistory);
                 }
                 break;
        }
    }

    private void getTriggersAndCall(final String callUserId) {
        final EditText editText = new EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Custom triggers")
                .setMessage("Tell us some important words or topics you are gonna talk about")
                .setView(editText)
                .setPositiveButton("CALL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final List<String> triggers = new ArrayList<>(Arrays.asList(editText.getText().toString().split("\\s?,\\s?")));
                        triggers.removeAll(Arrays.asList(""));
                        CallActivity.createOutgoingCall(HomeActivity.this, callUserId, new HashMap<String, Object>() {{
                            if (triggers.size() == 0) {
                                triggers.add("NA");
                                Debug.log(triggers);
                            }
                            put(CallEntity.CallData.TRIGGERS, triggers);
                            put(CallEntity.CallData.NAME, getNameForCallableUserId(callUserId));
                        }});
                    }
                }).setNegativeButton("CANCEL", null)
                .show();
    }

    private List<String> getCallableEmails() {
        List<String> emails = new ArrayList<>();
        if (callableUsers == null) {
            return null;
        }
        for (CallUser callableUser : callableUsers) {
            emails.add(callableUser.getEmail());
        }
        return emails;
    }

    private String getCallIdForEmail(String email) {
        if (callableUsers == null) {
            return null;
        }
        for (CallUser callableUser : callableUsers) {
            if (callableUser.getEmail().equals(email)) {
                return callableUser.getId();
            }
        }
        return null;
    }

    private String getNameForCallableUserId(String userId) {
        for (CallUser callableUser : callableUsers) {
            if (callableUser.getId().equals(userId)) {
                return callableUser.getName();
            }
        }
        return "Unknown";
    }
}
