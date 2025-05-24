// File: app/src/main/java/com/example/reuvenbagrut/adapters/ChatAdapter.java
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
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_SENT     = 1;
    private static final int TYPE_RECEIVED = 2;

    private final List<ChatMessage> messages;
    private final String            currentUid;
    private final SimpleDateFormat  timeFmt = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public ChatAdapter(List<ChatMessage> messages, String currentUid) {
        this.messages   = messages;
        this.currentUid = currentUid;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUid)
                ? TYPE_SENT
                : TYPE_RECEIVED;
    }

    @NonNull @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == TYPE_SENT
                ? R.layout.item_chat_sent
                : R.layout.item_chat_received;
        View view = LayoutInflater.from(parent.getContext())
                .inflate(layout, parent, false);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int pos) {
        MessageHolder h = (MessageHolder) holder;
        ChatMessage  msg = messages.get(pos);
        h.txtMessage.setText(msg.getMessage());      // <-- use getMessage()
        h.txtTime   .setText(timeFmt.format(msg.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageHolder extends RecyclerView.ViewHolder {
        TextView txtMessage, txtTime;
        MessageHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtTime    = itemView.findViewById(R.id.txtTime);
        }
    }
}
