package com.example.reuvenbagrut.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.ChatPreview;   // ‎↱ ‎דגם שמייצג צ'אט ברשימת-הצ'אטים
import de.hdodenhof.circleimageview.CircleImageView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.util.Log;
import java.util.TimeZone;

/**
 * Adapter לרשימת הצ'אטים (מסך ChatList) – מציג
 * שם משתמש, הודעה אחרונה ושעה.
 */
public class ChatMessageAdapter
        extends RecyclerView.Adapter<ChatMessageAdapter.VH> {

    private final List<ChatPreview> chats;
    private final SimpleDateFormat  fmt =
            new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final OnChatClick       listener;

    /** callback כשנלחץ צ'אט */
    public interface OnChatClick { void onClick(ChatPreview chat); }

    public ChatMessageAdapter(List<ChatPreview> chats, OnChatClick listener) {
        this.chats     = chats;
        this.listener  = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_preview, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        ChatPreview c = chats.get(pos);

        String name = c.getOtherName();
        if (name == null || name.trim().isEmpty()) name = "Unknown";
        String message = c.getLastMessage();
        if (message == null || message.trim().isEmpty()) message = "No messages yet";
        long time = c.getLastMessageTime();
        String profileImageUrl = c.getProfileImageUrl();
        
        // Format the timestamp to show local time
        String timeStr = "";
        if (time > 0) {
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getDefault());
            timeStr = sdf.format(date);
            Log.d("ChatAdapterDebug", "Raw timestamp: " + time + ", Formatted time: " + timeStr);
        }

        Log.d("ChatAdapterDebug", "Name: " + name + ", Message: " + message + ", Time: " + timeStr);

        h.tvName.setText(name);
        h.tvMessage.setText(message);
        h.tvTime.setText(timeStr);

        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(profileImageUrl, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(h.ivProfile.getContext())
                    .load(decodedByte)
                    .circleCrop()
                    .into(h.ivProfile);
            } catch (IllegalArgumentException e) {
                // Handle invalid Base64 string if necessary
                e.printStackTrace();
                Glide.with(h.ivProfile.getContext())
                    .load(R.drawable.ic_profile_placeholder)
                    .circleCrop()
                    .into(h.ivProfile);
            }
        } else {
            Glide.with(h.ivProfile.getContext())
                .load(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(h.ivProfile);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    /* ---------- ViewHolder ---------- */
    public static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvMessage, tvTime;
        final CircleImageView ivProfile;
        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvName);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime    = v.findViewById(R.id.tvTime);
            ivProfile = v.findViewById(R.id.ivProfile);
        }
    }
}
