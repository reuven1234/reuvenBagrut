package com.example.reuvenbagrut.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.ChatSummary;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private final Context context;
    private final List<ChatSummary> chats;
    private final OnChatClickListener listener;
    private final SimpleDateFormat dateFormat;

    public interface OnChatClickListener {
        void onChatClick(ChatSummary chat);
    }

    public ChatListAdapter(Context context, List<ChatSummary> chats, OnChatClickListener listener) {
        this.context = context;
        this.chats = chats;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM d, HH:mm", Locale.getDefault());
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatSummary chat = chats.get(position);
        
        // Set chat name
        holder.tvName.setText(chat.getOtherUserName() != null ? chat.getOtherUserName() : "Unknown User");
        
        // Set last message
        holder.tvLastMessage.setText(chat.getLastMessage() != null ? chat.getLastMessage() : "No messages yet");
        
        // Set timestamp
        if (chat.getLastMessageTime() != null) {
            holder.tvTimestamp.setText(dateFormat.format(new Date(chat.getLastMessageTime())));
        } else {
            holder.tvTimestamp.setText("");
        }
        
        // Load profile image
        if (chat.getOtherUserImage() != null && !chat.getOtherUserImage().isEmpty()) {
            Glide.with(context)
                .load(chat.getOtherUserImage())
                .circleCrop()
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(holder.ivProfile);
        } else {
            holder.ivProfile.setImageResource(R.drawable.ic_profile_placeholder);
        }
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvName;
        TextView tvLastMessage;
        TextView tvTimestamp;
        
        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvName = itemView.findViewById(R.id.tvName);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
} 