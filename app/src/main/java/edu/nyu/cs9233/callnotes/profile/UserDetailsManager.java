package edu.nyu.cs9233.callnotes.profile;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.amazonaws.amplify.generated.graphql.CreateCallMutation;
import com.amazonaws.amplify.generated.graphql.CreateUserDetailsMutation;
import com.amazonaws.amplify.generated.graphql.GetCallNoteQuery;
import com.amazonaws.amplify.generated.graphql.GetUserDetailsQuery;
import com.amazonaws.amplify.generated.graphql.ListCallNotesQuery;
import com.amazonaws.amplify.generated.graphql.ListUserDetailssQuery;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobileconnectors.apigateway.ApiClientFactory;
import com.amazonaws.mobileconnectors.apigateway.ApiRequest;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.calls.CallEntity;
import edu.nyu.cs9233.callnotes.home.CallHistory;
import edu.nyu.cs9233.callnotes.home.CallUser;
import edu.nyu.cs9233.callnotes.utils.ClientFactory;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import searchcalls.SearchcallsClient;
import type.CreateUserDetailsInput;
import type.ModelCallNoteFilterInput;

public class UserDetailsManager {

    private final AWSAppSyncClient awsAppSyncClient;
    private final CurrentCognitoUser cognitoUser;

    public UserDetailsManager(Context context, CurrentCognitoUser currentCognitoUser) {
        awsAppSyncClient = ClientFactory.createAppSyncClient(context);
        this.cognitoUser = currentCognitoUser;
    }

    public CurrentCognitoUser getCurrentCognitoUser() {
        return cognitoUser;
    }

    public void getAllUsers(final Listeners.GetObjectListener<List<CallUser>> listener) {
        ListUserDetailssQuery listUserDetailssQuery = ListUserDetailssQuery.builder().build();

        awsAppSyncClient.query(listUserDetailssQuery)
                .responseFetcher(AppSyncResponseFetchers.NETWORK_FIRST)
                .enqueue(new GraphQLCall.Callback<ListUserDetailssQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<ListUserDetailssQuery.Data> response) {
                final List<CallUser> callUsers = new ArrayList<>(10);
                for (ListUserDetailssQuery.Item item : response.data().listUserDetailss().items()) {
                    callUsers.add(new CallUser(item.name(), item.email(), item.id()));
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onSuccess(callUsers);
                    }
                });
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                listener.onFailure(e);
            }
        });
    }

    public void getUser(final Listeners.GetObjectListener<CallUser> listener) {
        getUser(cognitoUser.getSub(), listener);
    }

    public void getUser(String userId, final Listeners.GetObjectListener<CallUser> listener) {
        GetUserDetailsQuery getUserDetailsQuery = GetUserDetailsQuery.builder()
                .id(userId)
                .build();
        awsAppSyncClient.query(getUserDetailsQuery).enqueue(new GraphQLCall.Callback<GetUserDetailsQuery.Data>() {
            @Override
            public void onResponse(@Nonnull Response<GetUserDetailsQuery.Data> response) {
                if (response.data() == null || response.data().getUserDetails() == null) {
                    listener.onSuccess(null);
                    return;
                }
                GetUserDetailsQuery.GetUserDetails item = response.data().getUserDetails();
                listener.onSuccess(new CallUser(item.name(), item.email(), item.id()));
            }

            @Override
            public void onFailure(@Nonnull ApolloException e) {
                listener.onFailure(e);
            }
        });
    }

    public void createUserDetails(final Listeners.PersistObjectListener listener) {
        final CreateUserDetailsMutation createUserDetailsMutation = CreateUserDetailsMutation.builder()
                .input(CreateUserDetailsInput.builder()
                        .id(cognitoUser.getSub())
                        .name(cognitoUser.getName())
                        .email(cognitoUser.getEmail())
                        .build())
                .build();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                awsAppSyncClient.mutate(createUserDetailsMutation).enqueue(new GraphQLCall.Callback<CreateUserDetailsMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<CreateUserDetailsMutation.Data> response) {
                        if (listener != null) listener.onSuccess();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        if (listener != null) listener.onFailure(e);
                    }
                });
            }
        });
    }

    public void fetchCallNotes(final String callId, final Listeners.GetObjectListener<CallHistory> listener) {
        GetCallNoteQuery query = GetCallNoteQuery.builder().id(callId).build();
        awsAppSyncClient.query(query)
                .enqueue(new GraphQLCall.Callback<GetCallNoteQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<GetCallNoteQuery.Data> response) {
                        if (response.data() == null || response.data().getCallNote() == null) {
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onSuccess(null);
                                }
                            });
                        } else {
                            final GetCallNoteQuery.GetCallNote callNote = response.data().getCallNote();
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    listener.onSuccess(new CallHistory(callNote.id(), callNote.transcription(), callNote.summary()));
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        listener.onFailure(e);
                    }
                });
    }

}
