package edu.nyu.cs9233.callnotes.calls;


import java.util.Map;

import edu.nyu.cs9233.callnotes.utils.Listeners;

public abstract class CallManager {

    public abstract Dialer dialer();

    public abstract Receiver receiver();

    public interface Dialer {
        void requestCall(String uid, Map<String, Object> extraData, CallRequestEventListener listener);

        void endCall();

        void listenToCallReceiverStatus(String callId, CallReceiverListener listener);

        void setCallDialerStatus(String callId, String status, Listeners.PersistObjectListener listener);

        void enableSpeaker(boolean isEnabled);

        void startAudioRecording(String callId);

        void stopCallRecording();
    }

    public interface Receiver {

        void receiveCall(String callId, Listeners.PersistObjectListener listener);

        void listenToCallDialer(String callId, CallDialerListener listener);

        void setCallReceiverStatus(String callId, String status, Listeners.PersistObjectListener listener);

        void endCall();

        void enableSpeaker(boolean isEnabled);
    }

    interface CallRequestEventListener {

        void onCallRequestPermitted(String callId);

        void onCallRequestRejected(String errorMessage);
    }

    interface CallReceiverListener {

        void onReceiverStatusChange(String status);

        void onError(Exception e);
    }

    interface CallDialerListener {

        void onDialerStatusChange(String status);

        void onError(Exception e);
    }
}
