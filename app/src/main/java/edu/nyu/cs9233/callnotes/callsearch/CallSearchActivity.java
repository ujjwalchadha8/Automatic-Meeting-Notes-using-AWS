package edu.nyu.cs9233.callnotes.callsearch;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.calls.CallEntity;
import edu.nyu.cs9233.callnotes.calls.CallItemDBManager;
import edu.nyu.cs9233.callnotes.calls.CallSearchManager;
import edu.nyu.cs9233.callnotes.home.CallHistoryAdapter;
import edu.nyu.cs9233.callnotes.home.CallUser;
import edu.nyu.cs9233.callnotes.home.HomeActivity;
import edu.nyu.cs9233.callnotes.profile.UserDetailsManager;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class CallSearchActivity extends AppCompatActivity implements View.OnClickListener {

    private UserDetailsManager userDetailsManager;
    private EditText searchEditText;
    private View searchButton;
    private ArrayList<CallUser> callableUsers;
    private ListView searchResultsListView;
    private CallHistoryAdapter callResultsAdapter;
    private CallSearchManager callSearchManager;
    private View progressBar;

    private List<CallEntity> callHistory;

    public static void start(Context context, List<CallEntity> history) {
        Intent intent = new Intent(context, CallSearchActivity.class);
        intent.putExtra("CallHistory", new ArrayList<>(history));
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_search);
        initUI();
        callSearchManager = new CallSearchManager();
        callHistory = (List<CallEntity>) getIntent().getSerializableExtra("CallHistory");
        CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
            @Override
            public void onSuccess(CurrentCognitoUser user) {
                UserDetailsManager userDetailsManager = new UserDetailsManager(CallSearchActivity.this, user);
                fetchCallableUsers(userDetailsManager);
                callResultsAdapter = new CallHistoryAdapter(CallSearchActivity.this, user.getSub(), new ArrayList<CallEntity>(), new ArrayList<CallUser>());
                searchResultsListView.setAdapter(callResultsAdapter);
                CallSearchActivity.this.userDetailsManager = userDetailsManager;
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(CallSearchActivity.this, e.getMessage());
            }
        });
    }

    private void initUI() {
        searchEditText = findViewById(R.id.callSearchText);
        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(this);
        searchResultsListView = findViewById(R.id.callSearchResultsListView);
        progressBar = findViewById(R.id.progressBar);
    }

    private void fetchCallableUsers(final UserDetailsManager userDetailsManager) {
        userDetailsManager.getAllUsers(new Listeners.GetObjectListener<List<CallUser>>() {
            @Override
            public void onSuccess(List<CallUser> users) {
                Debug.log("FETCHED USER LIST: ", users);
                callableUsers = new ArrayList<>();
                for (CallUser user : users) {
                    if (!userDetailsManager.getCurrentCognitoUser().getEmail().equals(user.getEmail())) {
                        callResultsAdapter.addAllCallUsers(users);
                        callableUsers.add(user);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                Utils.showErrorAlert(CallSearchActivity.this, e.getMessage());
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.searchButton:
                if (searchEditText.getText().toString().trim().equals("") || userDetailsManager == null) {
                    return;
                }
                String text = searchEditText.getText().toString();
                callResultsAdapter.clear();
                progressBar.setVisibility(View.VISIBLE);
                callSearchManager.searchCalls(userDetailsManager.getCurrentCognitoUser().getSub() + " " + text, new Listeners.GetObjectListener<List<String>>() {
                    @Override
                    public void onSuccess(List<String> results) {
                        Debug.log("SEARCH RESULTS: ", results);
                        List<CallEntity> result = new ArrayList<>();
                        for (CallEntity callEntity : callHistory) {
                            if (results.contains(callEntity.getCallId())) {
                                result.add(callEntity);
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        callResultsAdapter.addAll(result);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Utils.showErrorAlert(CallSearchActivity.this, e.getMessage());
                        progressBar.setVisibility(View.GONE);
                    }
                });
                break;
        }
    }
}
