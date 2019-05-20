package edu.nyu.cs9233.callnotes.calls;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.amazonaws.amplify.generated.graphql.CreateCallMutation;
import com.amazonaws.amplify.generated.graphql.GetCallNoteQuery;
import com.amazonaws.amplify.generated.graphql.ListCallsQuery;
import com.amazonaws.amplify.generated.graphql.OnCreateCallSubscription;
import com.amazonaws.amplify.generated.graphql.OnUpdateCallSubscription;
import com.amazonaws.amplify.generated.graphql.UpdateCallMutation;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.AppSyncSubscriptionCall;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.utils.ClientFactory;
import edu.nyu.cs9233.callnotes.utils.Listeners;
import type.CreateCallInput;
import type.ModelCallFilterInput;
import type.ModelStringFilterInput;
import type.UpdateCallInput;

public class CallItemDBManager {

    private final AWSAppSyncClient awsAppSyncClient;

    public CallItemDBManager(Context context) {
        this.awsAppSyncClient = ClientFactory.createAppSyncClient(context);
    }

    public void createCall(final String dialerId, final String receiverId, Map<String, Object> data,
                                final Listeners.GetObjectListener<CallEntity> listener) {
        List<String> triggers = (List<String>) data.get(CallEntity.CallData.TRIGGERS);
        if (triggers == null) {
            triggers = new ArrayList<>();
        }
        final CreateCallMutation createCallMutation = CreateCallMutation.builder()
                .input(CreateCallInput.builder()
                        .dialerId(dialerId)
                        .receiverId(receiverId)
                        .receiverStatus(CallEntity.ReceiverStatus.CONNECTING)
                        .dialerStatus(CallEntity.DialerStatus.CONNECTING)
                        .triggers(triggers)
                        .build())
                .build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                awsAppSyncClient.mutate(createCallMutation).enqueue(new GraphQLCall.Callback<CreateCallMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull final Response<CreateCallMutation.Data> response) {
                        if (listener == null) {
                            return;
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(new CallEntity(
                                        response.data().createCall().id(),
                                        response.data().createCall().receiverId(),
                                        response.data().createCall().dialerId(),
                                        response.data().createCall().receiverStatus(),
                                        response.data().createCall().dialerStatus(),
                                        response.data().createCall().createdAt()));
                            }
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        listener.onFailure(e);
                    }
                });
            }
        });
    }


    public void fetchCallHistory(String uid, final Listeners.GetObjectListener<List<CallEntity>> listener) {
        List<ModelCallFilterInput> callFilters = new ArrayList<>();
        callFilters.add(ModelCallFilterInput.builder()
                .receiverId(ModelStringFilterInput.builder().eq(uid).build())
                .build());

        callFilters.add(ModelCallFilterInput.builder()
                .dialerId(ModelStringFilterInput.builder().eq(uid).build())
                .build());

        ListCallsQuery query = ListCallsQuery.builder()
                .limit(100)
                .filter(ModelCallFilterInput.builder().or(callFilters).build())
                .build();

        awsAppSyncClient.query(query)
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(new GraphQLCall.Callback<ListCallsQuery.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<ListCallsQuery.Data> response) {
                        final List<CallEntity> calls = new ArrayList<>();
                        for (ListCallsQuery.Item item : response.data().listCalls().items()) {
                            calls.add(new CallEntity(item.id(), item.receiverId(), item.dialerId(), item.receiverStatus(), item.dialerStatus(), item.createdAt()));
                        }
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                listener.onSuccess(calls);
                            }
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        listener.onFailure(e);
                    }
                });
    }

    public void listenToCall(final String callId, final Listeners.ChangeListener<CallEntity> listener) {
        if (listener == null) {
            return;
        }

        new Handler(Looper.getMainLooper()).post(new Runnable() {
             @Override
             public void run() {
                 awsAppSyncClient.subscribe(OnUpdateCallSubscription.builder().build()).execute(new AppSyncSubscriptionCall.Callback<OnUpdateCallSubscription.Data>() {
                     @Override
                     public void onResponse(@Nonnull final Response<OnUpdateCallSubscription.Data> response) {
                         OnUpdateCallSubscription.OnUpdateCall onUpdateCallEntity = response.data() == null ? null : response.data().onUpdateCall();
                         if (onUpdateCallEntity == null) {
                             listener.onChange(null);
                             return;
                         }
                         if (!onUpdateCallEntity.id().equals(callId)) {
                             return;
                         }
                         listener.onChange(new CallEntity(
                                 onUpdateCallEntity.id(),
                                 onUpdateCallEntity.receiverId(),
                                 onUpdateCallEntity.dialerId(),
                                 onUpdateCallEntity.receiverStatus(),
                                 onUpdateCallEntity.dialerStatus(),
                                 onUpdateCallEntity.createdAt()));
                     }

                     @Override
                     public void onFailure(@Nonnull ApolloException e) {
                         listener.onFailure(e);
                     }

                     @Override
                     public void onCompleted() {
                         listener.onCompleted(null);
                     }
                 });
             }
         });

    }

    public void setCallDialerStatus(String callId, String dialerStatus, final Listeners.PersistObjectListener listener) {
        final UpdateCallMutation mutation = UpdateCallMutation.builder()
                .input(UpdateCallInput.builder()
                        .id(callId)
                        .dialerStatus(dialerStatus)
                        .build())
                .build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                awsAppSyncClient.mutate(mutation).enqueue(new GraphQLCall.Callback<UpdateCallMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<UpdateCallMutation.Data> response) {
                        if (listener == null) {
                            return;
                        }
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        if (listener == null) {
                            return;
                        }
                        listener.onFailure(e);
                    }
                });
            }
        });

    }

    public void setCallReceiverStatus(String callId, String receiverStatus, final Listeners.PersistObjectListener listener) {
        final UpdateCallMutation mutation = UpdateCallMutation.builder()
                .input(UpdateCallInput.builder()
                        .id(callId)
                        .receiverStatus(receiverStatus)
                        .build())
                .build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                awsAppSyncClient.mutate(mutation).enqueue(new GraphQLCall.Callback<UpdateCallMutation.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<UpdateCallMutation.Data> response) {
                        if (listener == null) {
                            return;
                        }
                        listener.onSuccess();
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        if (listener == null) {
                            return;
                        }
                        listener.onFailure(e);
                    }
                });
            }
        });

    }

    public void listenToIncomingCalls(final Listeners.ChangeListener<CallEntity> listener) {
        final OnCreateCallSubscription subscription = OnCreateCallSubscription.builder().build();
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                awsAppSyncClient.subscribe(subscription).execute(new AppSyncSubscriptionCall.Callback<OnCreateCallSubscription.Data>() {
                    @Override
                    public void onResponse(@Nonnull Response<OnCreateCallSubscription.Data> response) {
                        final OnCreateCallSubscription.OnCreateCall onCreateCall = response.data().onCreateCall();
                        CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
                            @Override
                            public void onSuccess(CurrentCognitoUser user) {
                                if (onCreateCall.receiverId().equals(user.getSub())) {
                                    listener.onChange(new CallEntity(
                                            onCreateCall.id(),
                                            onCreateCall.receiverId(),
                                            onCreateCall.dialerId(),
                                            onCreateCall.receiverStatus(),
                                            onCreateCall.dialerStatus(),
                                            onCreateCall.createdAt()));
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                listener.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e) {
                        listener.onFailure(e);
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
            }
        });

    }

}
