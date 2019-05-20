package edu.nyu.cs9233.callnotes.calls;

import java.util.HashMap;
import java.util.Map;

public class CallsContract {

    public interface Presenter{
        void onInitCallReceived(String callId, HashMap<String, String> callData);
        void onInitCallCreate(String uid, Map<String, Object> extraData);
        void onMuteCall(boolean isMuted);
        void onSpeakerPhoneEnabled(boolean isEnabled);
        void onEndCall();
        void onAnswerCall();

        void onDeclineCall();

        void onMuteEnabled(boolean isEnabled);

        void onMessage();
    }

    public interface View{

        int CONNECTING_SOUND = 0, RING_SOUND = 1, BUSY_SOUND = 2, CALL_END_SOUND = 3;

        void createIncomingCallView();

        void createOnCallView();

        void startRinging();

        void stopRinging();

        void setStatus(String status);


        void setSpeakerMode(boolean isSpeakerOn);

        void playSignalingSound(int sound);

        void stopSignalingSound();

        void finish();

        void setName(String name);

        void setPhoto(String photoUrl);

        void showError(String call_failed);

    }

}
