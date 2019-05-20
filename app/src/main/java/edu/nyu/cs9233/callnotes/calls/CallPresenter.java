package edu.nyu.cs9233.callnotes.calls;

import android.content.Context;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;

import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.FileUploader;
import edu.nyu.cs9233.callnotes.utils.Listeners;

public class CallPresenter implements CallsContract.Presenter {
    public static final int CALL_TYPE_INCOMING = 0, CALL_TYPE_OUTGOING = 1;
    private CallsContract.View view;
    private CallManager callManager;

    private int callType;
    private String callId;
    private boolean isCallEnded = false;

    private Context context;

    public CallPresenter(CallsContract.View view, Context context){
        this.view = view;
        this.context = context;
        callManager = new AgoraCallManager(context);
    }

    @Override
    public void onInitCallReceived(final String callId, HashMap<String, String> callData) {
        this.callId = callId;
        callType = CALL_TYPE_INCOMING;
        view.createIncomingCallView();
        view.setName(callData.get("name"));
        view.setPhoto(callData.get("photoUrl"));
        callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.RINGING, handleStatusPersist());
        callManager.receiver().listenToCallDialer(callId, new CallManager.CallDialerListener() {
            @Override
            public void onDialerStatusChange(String status) {
                switch (status){
                    case CallEntity.DialerStatus.CONNECTING:
                        view.setStatus("connecting");
                        break;
                    case CallEntity.DialerStatus.ON_CALL:
//                        view.setStatus("Incoming call");
//                        view.createIncomingCallView();
//                        view.startRinging();
//                        callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.RINGING, handleStatusPersist());
                        break;
                    case CallEntity.DialerStatus.CALL_ENDED:
                        view.setStatus("Call Ended");
                        view.playSignalingSound(CallsContract.View.CALL_END_SOUND);
                        view.stopSignalingSound();
                        callManager.receiver().endCall();
                        callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.CALL_ENDED, handleStatusPersist());
                        view.finish();
                        break;
                }
            }

            @Override
            public void onError(Exception e) {
                //TODO
                e.printStackTrace();
                view.showError("Call Failed");
            }
        });
    }

    @Override
    public void onInitCallCreate(String uid, final Map<String, Object> extraData) {
        callType = CALL_TYPE_OUTGOING;
        view.createOnCallView();
        view.setStatus("calling");
        Debug.log("CALLING DATA: ", extraData);
        callManager.dialer().requestCall(uid, extraData, new CallManager.CallRequestEventListener() {
            @Override
            public void onCallRequestPermitted(final String callId) {

                CallPresenter.this.callId = callId;
                if(isCallEnded) { onEndCall(); return; }

                view.setStatus("connecting");
                view.setName((String) extraData.get(CallEntity.CallData.NAME));
                Debug.log("CONNECTING!");
//                view.playSignalingSound(CallsContract.View.CONNECTING_SOUND);
                callManager.dialer().setCallDialerStatus(callId, CallEntity.DialerStatus.ON_CALL, handleStatusPersist());
                callManager.dialer().listenToCallReceiverStatus(callId, new CallManager.CallReceiverListener() {
                    @Override
                    public void onReceiverStatusChange(String status) {
                        switch (status){
                            case CallEntity.ReceiverStatus.RINGING:
                                view.setStatus("Ringing");
                                view.playSignalingSound(CallsContract.View.RING_SOUND);
                                break;
                            case CallEntity.ReceiverStatus.JOINING_CALL:
                                view.stopSignalingSound();
//                                view.setStatus("Connecting");
                                break;
                            case CallEntity.ReceiverStatus.ON_CALL:
                                view.stopSignalingSound();
                                view.setStatus("On Call");
                                callManager.dialer().startAudioRecording(callId);
                                break;
                            case CallEntity.ReceiverStatus.CALL_ENDED:
                                view.stopSignalingSound();
                                view.setStatus("Call Ended");
                                callManager.dialer().setCallDialerStatus(callId, CallEntity.DialerStatus.CALL_ENDED, handleStatusPersist());
                                view.playSignalingSound(CallsContract.View.CALL_END_SOUND);
                                view.stopSignalingSound();
                                callManager.dialer().stopCallRecording();
                                uploadRecording(callId);
                                view.finish();
                                break;
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }

            @Override
            public void onCallRequestRejected(String errorMessage){
                throw new RuntimeException("Error: "+ errorMessage);
            }
        });
    }

    public void uploadRecording(final String callId) {
        new Handler(context.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                new FileUploader(context).uploadFromInternalStorage(callId + ".wav", new Listeners.PersistObjectListener() {
                    @Override
                    public void onSuccess() { }

                    @Override
                    public void onFailure(Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }, 1000);

    }

    private Listeners.PersistObjectListener handleStatusPersist(){
        return new Listeners.PersistObjectListener() {
            @Override
            public void onSuccess() {}

            @Override
            public void onFailure(Exception e) {
                //TODO
                view.showError("Please check your internet connection");
            }
        };
    }

    @Override
    public void onMuteCall(boolean isMuted) {

    }

    @Override
    public void onSpeakerPhoneEnabled(boolean isEnabled) {
        switch (callType){
            case CALL_TYPE_INCOMING:
                callManager.receiver().enableSpeaker(isEnabled);
                break;
            case CALL_TYPE_OUTGOING:
                callManager.receiver().enableSpeaker(isEnabled);
                break;
        }
    }

    @Override
    public void onAnswerCall() {
        view.createOnCallView();
        view.stopRinging();
        view.setStatus("On Call");
        callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.JOINING_CALL, handleStatusPersist());
        callManager.receiver().receiveCall(callId, new Listeners.PersistObjectListener() {
            @Override
            public void onSuccess() {
                callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.ON_CALL, handleStatusPersist());
            }

            @Override
            public void onFailure(Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void onEndCall() {
        switch (callType){
            case CALL_TYPE_INCOMING:
                callManager.receiver().endCall();
                callManager.receiver().setCallReceiverStatus(callId, CallEntity.ReceiverStatus.CALL_ENDED, handleStatusPersist());
                view.playSignalingSound(CallsContract.View.CALL_END_SOUND);
                view.finish();
                break;
            case CALL_TYPE_OUTGOING:
                if(callId == null){
                    view.finish(); return;
                }
                callManager.dialer().endCall();
                callManager.dialer().setCallDialerStatus(callId, CallEntity.DialerStatus.CALL_ENDED, handleStatusPersist());
                callManager.dialer().stopCallRecording();
                view.playSignalingSound(CallsContract.View.CALL_END_SOUND);
                view.finish();
                uploadRecording(callId);
                break;
        }
        isCallEnded = true;
    }

    @Override
    public void onDeclineCall() {
        onEndCall();
    }

    @Override
    public void onMuteEnabled(boolean isEnabled) {

    }

    @Override
    public void onMessage() {

    }
}
