package edu.nyu.cs9233.callnotes.calls;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CallEntity implements Serializable {

    private final String callId;
    private final String receiverId;
    private final String dialerId;
    private final String receiverStatus;
    private final String dialerStatus;
    private final DateTime createdAt;

    public static class DialerStatus {
        public static final String CONNECTING = "connecting",
                ON_CALL = "onCall",
                CALL_ENDED = "callEnded";
    }

    public static class ReceiverStatus {
        public static final String CONNECTING = "connecting",
                RINGING = "ringing",
                JOINING_CALL = "joiningCall",
                ON_CALL = "onCall",
                CALL_ENDED = "callEnded";
    }

    public static class CallData {
        public static final String TRIGGERS = "TRIGGERS";
        public static final String NAME = "NAME";
    }

    public CallEntity(String callId, String receiverId, String dialerId, String receiverStatus, String dialerStatus, String createdAt) {
        this.callId = callId;
        this.receiverId = receiverId;
        this.dialerId = dialerId;
        this.receiverStatus = receiverStatus;
        this.dialerStatus = dialerStatus;
        this.createdAt = DateTime.parse(createdAt);
    }

    public String getCallId() {
        return callId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getDialerId() {
        return dialerId;
    }

    public String getReceiverStatus() {
        return receiverStatus;
    }

    public String getDialerStatus() {
        return dialerStatus;
    }

    public DateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "CallEntity{" +
                "callId='" + callId + '\'' +
                ", receiverId='" + receiverId + '\'' +
                ", dialerId='" + dialerId + '\'' +
                ", receiverStatus='" + receiverStatus + '\'' +
                ", dialerStatus='" + dialerStatus + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }

    public static List<CallEntity> sortByDescendingTimestamp(List<CallEntity> callEntities) {
        List<CallEntity> sorted = new ArrayList<>(callEntities);
        Collections.sort(sorted, new Comparator<CallEntity>() {
            @Override
            public int compare(CallEntity c1, CallEntity c2) {
                return -1 *c1.getCreatedAt().compareTo(c2.getCreatedAt());
            }
        });
        return sorted;
    }
}
