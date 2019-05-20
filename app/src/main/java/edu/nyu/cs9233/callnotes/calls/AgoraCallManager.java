package edu.nyu.cs9233.callnotes.calls;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.authentication.CurrentCognitoUser;
import edu.nyu.cs9233.callnotes.utils.Debug;
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;

import edu.nyu.cs9233.callnotes.utils.Listeners;


public class AgoraCallManager extends CallManager {

    private final CallManager.Dialer dialer;
    private final CallManager.Receiver receiver;

    public AgoraCallManager(Context context){
        dialer = new AgoraDialer(context);
        receiver = new AgoraReceiver(context);
    }

    @Override
    public Dialer dialer() {
        return dialer;
    }

    @Override
    public Receiver receiver() {
        return receiver;
    }

    public static class AgoraDialer implements Dialer {

        private final Context context;
        private final RtcEngine mRtcEngine;
        private final CallItemDBManager callItemDBManager;

        private AgoraDialer(Context context) {
            this.context = context;
            this.callItemDBManager = new CallItemDBManager(context);

            try {
                mRtcEngine = RtcEngine.create(context, context.getString(R.string.agoraAppId), new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int joinedUid, int elapsed) {
                        super.onJoinChannelSuccess(channel, joinedUid, elapsed);
                        Debug.log("Join channel success: " + channel);
                        try {
//                            listener.onCallRequestPermitted("");
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onLeaveChannel(RtcStats stats) {
                        super.onLeaveChannel(stats);
                        Debug.log("Leave channel success: ");
                    }

                    @Override
                    public void onError(int err) {
                        super.onError(err);
//                        listener.onCallRequestRejected("AGORA_ERROR: "+ err);
                        Debug.log("Error: " + err);
                    }

                });
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        /**
         * @param uid
         * @param listener
         * 1. call server function to request call. dialerStatus will initially be connecting.
         * 2. server will verify and send response. Along with sending FCM to receiver.
         * 3. on success joinChannel
         * 4. on failure, pass the error.
         * 5. on channel joined, pass success to presenter //set dialer status to onCall is another command that presenter will call on success
         */
        @Override
        public void requestCall(final String uid, final Map<String, Object> extraData, final CallRequestEventListener listener) {
            CurrentCognitoUser.load(new Listeners.GetObjectListener<CurrentCognitoUser>() {
                @Override
                public void onSuccess(CurrentCognitoUser user) {
                    callItemDBManager.createCall(user.getSub(), uid, extraData, new Listeners.GetObjectListener<CallEntity>() {
                        @Override
                        public void onSuccess(CallEntity callEntity) {
                            mRtcEngine.enableAudio();
                            mRtcEngine.setEnableSpeakerphone(true);
                            mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_DEFAULT);
                            Debug.log("JOINING CALL: " + callEntity.getCallId());
                            mRtcEngine.joinChannel(null, callEntity.getCallId(), "", new Random().nextInt());
                            listener.onCallRequestPermitted(callEntity.getCallId());
                        }

                        @Override
                        public void onFailure(Exception e) {
                            listener.onCallRequestRejected(e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onCallRequestRejected(e.getMessage());
                }
            });

        }

        @Override
        public void endCall(){
            mRtcEngine.leaveChannel();
        }

        /**
         * @param listener
         * 1. set listener on call document change.
         * 2. Monitor states and send the corresponding states to the presenter.
         */
        @Override
        public void listenToCallReceiverStatus(final String callId, final CallReceiverListener listener) {
            callItemDBManager.listenToCall(callId, new Listeners.ChangeListener<CallEntity>() {

                private String receiverStatus;

                @Override
                public void onChange(CallEntity entity) {
                    Debug.log("RECEIVER STATUS CHANGED: ", entity);
                    if (!entity.getReceiverStatus().equals(receiverStatus)) {
                        listener.onReceiverStatusChange(entity.getReceiverStatus());
                        receiverStatus = entity.getReceiverStatus();
                        Debug.log("TRANSFERRING RECEIVER STATUS");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onError(e);
                }

                @Override
                public void onCompleted(CallEntity entity) {

                }
            });
        }

        /**
         * @param status
         * @param listener
         * call server function to change the dialer status.
         */
        @Override
        public void setCallDialerStatus(String callId, String status, final Listeners.PersistObjectListener listener) {
            callItemDBManager.setCallDialerStatus(callId, status, listener);
        }

        @Override
        public void enableSpeaker(boolean isEnabled) {
            mRtcEngine.setEnableSpeakerphone(isEnabled);
        }

        @Override
        public void startAudioRecording(String callId) {
            mRtcEngine.startAudioRecording(new File(context.getFilesDir(), callId).getPath() + ".wav", IRtcEngineEventHandler.Quality.GOOD);
            Debug.log("CALL RECORD START");
        }

        @Override
        public void stopCallRecording() {
            mRtcEngine.stopAudioRecording();
            Debug.log("CALL RECORD END");
        }

    }

    public class AgoraReceiver implements Receiver{

        private final RtcEngine mRtcEngine;
        private final Context context;
        private final CallItemDBManager callItemDBManager;

        private AgoraReceiver(Context context) {
            this.context = context;
            this.callItemDBManager = new CallItemDBManager(context);

            try {
                mRtcEngine = RtcEngine.create(context, context.getString(R.string.agoraAppId), new IRtcEngineEventHandler() {
                    @Override
                    public void onJoinChannelSuccess(String channel, int joinedUid, int elapsed) {
                        super.onJoinChannelSuccess(channel, joinedUid, elapsed);
                        Debug.log("Join channel success: " + channel);
                        try {
//                            listener.onCallRequestPermitted("");
                        }catch (Exception e){
                            throw new RuntimeException(e);
                        }
                    }

                    @Override
                    public void onLeaveChannel(RtcStats stats) {
                        super.onLeaveChannel(stats);
                        Debug.log("Leave channel success: ");
                    }

                    @Override
                    public void onError(int err) {
                        super.onError(err);
//                        listener.onCallRequestRejected("AGORA_ERROR: "+ err);
                        throw new RuntimeException("AGORA_ERROR: " + err);
                    }

                });
            } catch (Exception e){
                throw new RuntimeException(e);
            }
        }

        /**
         *
         * @param callId
         * request token for the given call id from server
         * on success join channel
         */
        @Override
        public void receiveCall(final String callId, final Listeners.PersistObjectListener listener){
            mRtcEngine.enableAudio();
            mRtcEngine.setEnableSpeakerphone(true);
            mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_SPEECH_STANDARD, Constants.AUDIO_SCENARIO_DEFAULT);
            mRtcEngine.joinChannel(null, callId, "", new Random().nextInt());
            Debug.log("JOINING CALL: " + callId);
            listener.onSuccess();
        }

        @Override
        public void listenToCallDialer(final String callId, final CallDialerListener listener) {
            callItemDBManager.listenToCall(callId, new Listeners.ChangeListener<CallEntity>() {
                private String dialerStatus;

                @Override
                public void onChange(CallEntity entity) {
                    Debug.log("DIALER STATUS CHANGED: ", entity);
                    if (!entity.getDialerStatus().equals(dialerStatus)) {
                        listener.onDialerStatusChange(entity.getDialerStatus());
                        dialerStatus = entity.getReceiverStatus();
                        Debug.log("TRANSFERRING DIALER STATUS");
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    listener.onError(e);
                }

                @Override
                public void onCompleted(CallEntity entity) {

                }
            });
        }

        @Override
        public void endCall() {
            if(mRtcEngine != null) {
                mRtcEngine.leaveChannel();
            }
        }

        @Override
        public void setCallReceiverStatus(String callId, String status, final Listeners.PersistObjectListener listener) {
            callItemDBManager.setCallReceiverStatus(callId, status, listener);
        }

        @Override
        public void enableSpeaker(boolean isEnabled) {
            mRtcEngine.setEnableSpeakerphone(isEnabled);
        }
    }
}
