package edu.nyu.cs9233.callnotes.home;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

import edu.nyu.cs9233.callnotes.R;
import edu.nyu.cs9233.callnotes.calls.CallEntity;
import edu.nyu.cs9233.callnotes.utils.Utils;

public class CallHistoryAdapter extends ArrayAdapter<CallEntity> {

    private final Context context;
    private final List<CallEntity> callEntities;
    private final HashMap<String, CallUser> userIdUserMap;
    private final String currentUserId;

    public CallHistoryAdapter(Context context, String currentUserId, List<CallEntity> callEntities, List<CallUser> callUsers) {
        super(context, R.layout.call_history_item, callEntities);
        this.currentUserId = currentUserId;
        this.context = context;
        this.callEntities = callEntities;
        userIdUserMap = new HashMap<>();
        for (CallUser callUser : callUsers) {
            userIdUserMap.put(callUser.getId(), callUser);
        }
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View root = LayoutInflater.from(context).inflate(R.layout.call_history_item, parent, false);
        TextView emailText =  root.findViewById(R.id.emailText);
        ImageView callTypeImage =  root.findViewById(R.id.callType);
        TextView callTimeText =  root.findViewById(R.id.callTime);

        final CallEntity callEntity = callEntities.get(position);
        final boolean isDialer = callEntity.getDialerId().equals(currentUserId);

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CallSummaryActivity.start(context, callEntity, isDialer ? userIdUserMap.get(callEntity.getReceiverId()) : userIdUserMap.get(callEntity.getDialerId()));
            }
        });


        if (isDialer) {
            callTypeImage.setImageDrawable(context.getDrawable(R.drawable.ic_call_made));
            if (userIdUserMap.get(callEntity.getReceiverId()) != null) {
                emailText.setText(userIdUserMap.get(callEntity.getReceiverId()).getEmail());
            } else {
                emailText.setText("Unknown caller");
            }
            callTimeText.setText(Utils.getAgoTimestamp(callEntity.getCreatedAt().toDate()));
        } else {
            callTypeImage.setImageDrawable(context.getDrawable(R.drawable.ic_call_received_black_24dp));
            if (userIdUserMap.get(callEntity.getDialerId()) != null) {
                emailText.setText(userIdUserMap.get(callEntity.getDialerId()).getEmail());
            } else {
                emailText.setText("Unknown caller");
            }
            callTimeText.setText(Utils.getAgoTimestamp(callEntity.getCreatedAt().toDate()));
        }
        return root;
    }

    public void addAll(List<CallEntity> callEntities) {
        this.callEntities.addAll(callEntities);
        notifyDataSetChanged();
    }

    public void addAllCallUsers(List<CallUser> callUsers) {
        for (CallUser callUser : callUsers) {
            userIdUserMap.put(callUser.getId(), callUser);
        }
        notifyDataSetChanged();
    }

    public void clear() {
        this.callEntities.clear();
        notifyDataSetChanged();
    }
}
