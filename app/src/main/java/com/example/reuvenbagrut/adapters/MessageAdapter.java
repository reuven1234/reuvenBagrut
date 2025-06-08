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

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private final List<ChatMessage> messages;
    private final String currentUserId;
    private final SimpleDateFormat timeFormat;
    
    public MessageAdapter(List<ChatMessage> messages, String currentUserId) {
        this.messages = messages;
        this.currentUserId = currentUserId;
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    @Override
    public int getItemViewType(int position) {
        ChatMessage message = messages.get(position);
        return message.getUserId().equals(currentUserId) ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == VIEW_TYPE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_sent, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message_received, parent, false);
        }
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        
        holder.tvMessage.setText(message.getMessage());
        
        if (message.getTimestamp() != 0) {
            holder.tvTime.setText(timeFormat.format(new Date(message.getTimestamp())));
        } else {
            holder.tvTime.setText("");
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTime;
        
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
} 