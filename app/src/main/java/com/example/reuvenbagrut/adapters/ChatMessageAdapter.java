package com.example.reuvenbagrut.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.ChatPreview;   // ‎↱ ‎דגם שמייצג צ'אט ברשימת-הצ'אטים

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

        h.tvName.setText(c.getOtherName());
        h.tvMessage.setText(c.getLastMessage());
        h.tvTime.setText(fmt.format(new Date(c.getLastMessageTime())));

        h.itemView.setOnClickListener(v -> listener.onClick(c));
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    /* ---------- ViewHolder ---------- */
    static class VH extends RecyclerView.ViewHolder {
        final TextView tvName, tvMessage, tvTime;
        VH(@NonNull View v) {
            super(v);
            tvName    = v.findViewById(R.id.tvName);
            tvMessage = v.findViewById(R.id.tvMessage);
            tvTime    = v.findViewById(R.id.tvTime);
        }
    }
}
