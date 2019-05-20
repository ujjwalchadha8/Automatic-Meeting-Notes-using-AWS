package edu.nyu.cs9233.callnotes.calls;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.utils.Debug;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class CallActivity extends AppCompatActivity implements CallsContract.View, View.OnClickListener {

    private TextView callerNameText;
    private TextView statusText;
    private ImageView callerImage;
    private View speakerButton;
    private View muteButton;
    private View messageButton;
    private View callEndButton;
    private View callDeclineButton;
    private View callAnswerButton;
    private TextView callAnswerLabel;
    private TextView callDeclineLabel;

    private CallsContract.Presenter presenter;
    private Ringtone ringtone;
    private MediaPlayer signalingTonePlayer;
    private boolean isSpeakerOn = false;

    public static void createOutgoingCall(Context context, String uid, HashMap<String, Object> extraData){
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("type", "outgoing");
        intent.putExtra("uid", uid);
        intent.putExtra("callData", extraData);
        context.startActivity(intent);
    }

    public static void createIncomingCall(Context context, String callId, HashMap<String, String> callData){
        Intent intent = new Intent(context, CallActivity.class);
        intent.putExtra("type", "incoming");
        intent.putExtra("callId", callId);
        intent.putExtra("callData", callData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.call_receiver_dialer);
        callerNameText = findViewById(R.id.callerName);
        statusText = findViewById(R.id.statusText);
        callAnswerButton = findViewById(R.id.answerCall);
        callDeclineButton = findViewById(R.id.declineCall);
        callerImage = findViewById(R.id.callerImage);
        speakerButton = findViewById(R.id.speaker);
        muteButton = findViewById(R.id.mute);
        messageButton = findViewById(R.id.message);
        callEndButton = findViewById(R.id.endCall);
        callAnswerButton = findViewById(R.id.answerCall);
        callDeclineButton = findViewById(R.id.declineCall);
        callAnswerLabel = findViewById(R.id.answerCallLabel);
        callDeclineLabel = findViewById(R.id.declineCallLabel);

        callAnswerButton.setOnClickListener(this);
        callDeclineLabel.setOnClickListener(this);
        callEndButton.setOnClickListener(this);
        speakerButton.setOnClickListener(this);
        muteButton.setOnClickListener(this);
        messageButton.setOnClickListener(this);

        Debug.log("RECORD PERMISSION: ", Utils.isPermissionGranted(Manifest.permission.RECORD_AUDIO, this));

        presenter = new CallPresenter(this, this);
        switch (getIntent().getStringExtra("type")){
            case "incoming":
                presenter.onInitCallReceived(getIntent().getStringExtra("callId"), (HashMap<String, String>) getIntent().getSerializableExtra("callData"));
                break;
            case "outgoing":
                presenter.onInitCallCreate(getIntent().getStringExtra("uid"), (Map<String, Object>) getIntent().getSerializableExtra("callData"));
                break;
            default:
                throw new AssertionError("Invalid call type: " + getIntent().getStringExtra("type"));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.answerCall:
                presenter.onAnswerCall();
                break;
            case R.id.declineCall:
                presenter.onDeclineCall();
                break;
            case R.id.endCall:
                presenter.onEndCall();
                break;
            case R.id.speaker:
                speakerButton.setSelected(!speakerButton.isSelected());
                presenter.onSpeakerPhoneEnabled(speakerButton.isSelected());
                //TODO: speaker button background
                if (speakerButton.isSelected()) {
                    speakerButton.setBackgroundColor(getColor(R.color.white));
                } else {
                    speakerButton.setBackgroundColor(getColor(R.color.colorPrimaryDark));
                }
                break;
            case R.id.mute:
                muteButton.setSelected(!muteButton.isSelected());
                presenter.onMuteEnabled(muteButton.isSelected());
                //TODO: mute button background
                break;
            case R.id.message:
                presenter.onMessage();
                break;
        }
    }

    @Override
    public void createIncomingCallView() {
        callAnswerButton.setVisibility(View.VISIBLE);
        callDeclineLabel.setVisibility(View.VISIBLE);
        callDeclineButton.setVisibility(View.VISIBLE);
        callDeclineLabel.setVisibility(View.VISIBLE);
        speakerButton.setVisibility(View.INVISIBLE);
        messageButton.setVisibility(View.INVISIBLE);
        muteButton.setVisibility(View.INVISIBLE);
        callEndButton.setVisibility(View.INVISIBLE);
        statusText.setText("Incoming");
    }

    @Override
    public void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void createOnCallView(){
        callAnswerButton.setVisibility(View.INVISIBLE);
        callDeclineLabel.setVisibility(View.INVISIBLE);
        callDeclineButton.setVisibility(View.INVISIBLE);
        callDeclineLabel.setVisibility(View.INVISIBLE);
        speakerButton.setVisibility(View.VISIBLE);
        messageButton.setVisibility(View.VISIBLE);
        muteButton.setVisibility(View.VISIBLE);
        callEndButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void setName(String name) {
        callerNameText.setText(name);
    }

    @Override
    public void setPhoto(String photoUrl) {
        //Glide.with(this).load(photoUrl).into(callerImage);
    }

    /**
     * check for silent
     * ring phone
     */
    @Override
    public void startRinging(){
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);
        ringtone.play();
    }

    @Override
    public void stopRinging(){
        if(ringtone != null && ringtone.isPlaying()) ringtone.stop();
    }

    @Override
    public void setSpeakerMode(boolean isSpeakerOn){
        this.isSpeakerOn = isSpeakerOn;
    }

    @Override
    public void playSignalingSound(int sound){
        stopSignalingSound();
        if (sound == RING_SOUND) {
            signalingTonePlayer = MediaPlayer.create(this, R.raw.ringing_3);
//            if (isSpeakerOn) playThroughSpeakers();
//            else playThroughEarpiece();
            playThroughEarpiece();
            signalingTonePlayer.start();
        }
    }

    @Override
    public void stopSignalingSound(){
        if(signalingTonePlayer != null && signalingTonePlayer.isPlaying()) {
            signalingTonePlayer.stop();
        }
    }

    private void playThroughEarpiece(){
        signalingTonePlayer.setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION_SIGNALLING)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());
    }

    private void playThroughSpeakers(){
        signalingTonePlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    public void setStatus(final String status){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
            }
        });
    }
}
