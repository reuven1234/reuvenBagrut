package com.example.reuvenbagrut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.VH> {

    private static final int SENT     = 1;
    private static final int RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String            currentUid;
    private final SimpleDateFormat  fmt =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MessageAdapter(List<ChatMessage> messages, String currentUid) {
        this.messages  = messages;
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int pos) {
        return messages.get(pos).getSenderId().equals(currentUid) ? SENT : RECEIVED;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(
                viewType == SENT
                        ? R.layout.item_message_sent
                        : R.layout.item_message_received,
                parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ChatMessage m = messages.get(pos);
        h.tvMsg.setText(m.getMessage());
        h.tvTime.setText(fmt.format(new Date(m.getTimestamp())));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvMsg, tvTime;
        VH(@NonNull View v) {
            super(v);
            tvMsg  = v.findViewById(R.id.tvMessage);
            tvTime = v.findViewById(R.id.tvTime);
        }
    }
}
