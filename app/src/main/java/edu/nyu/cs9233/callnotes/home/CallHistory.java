package edu.nyu.cs9233.callnotes.home;

public class CallHistory {

    private final String callId;
    private final String callTranscription;
    private final String callSummmary;

    public CallHistory(String callId, String callTranscription, String callSummmary) {
        this.callId = callId;
        this.callTranscription = callTranscription;
        this.callSummmary = callSummmary;
    }

    public String getCallId() {
        return callId;
    }

    public String getCallTranscription() {
        return callTranscription;
    }

    public String getCallSummmary() {
        return callSummmary;
    }
}
