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
import com.example.reuvenbagrut.models.ChatMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> {
    private Context context;
    private List<ChatMessage> messages;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatMessage message);
    }

    public ChatListAdapter(Context context, List<ChatMessage> messages, OnChatClickListener listener) {
        this.context = context;
        this.messages = messages;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        holder.userName.setText(message.getSenderName());
        holder.lastMessage.setText(message.getMessage());
        holder.timeText.setText(formatTime(message.getTimestamp()));

        if (message.getSenderImage() != null && !message.getSenderImage().isEmpty()) {
            Glide.with(context)
                .load(message.getSenderImage())
                .circleCrop()
                .into(holder.profileImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(message);
            }
        });
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    private String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName;
        TextView lastMessage;
        TextView timeText;

        ChatViewHolder(View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profileImage);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            timeText = itemView.findViewById(R.id.timeText);
        }
    }
} 