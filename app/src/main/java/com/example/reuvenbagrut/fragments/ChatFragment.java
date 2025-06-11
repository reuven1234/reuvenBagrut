package com.example.reuvenbagrut.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.MessageAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    /** Factory-method – ChatActivity משתמש בזה. */
    public static ChatFragment newInstance(String chatId,
                                           String otherUserId,
                                           String otherUserName) {
        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        args.putString("otherUid", otherUserId);
        args.putString("otherName", otherUserName);

        ChatFragment f = new ChatFragment();
        f.setArguments(args);
        return f;
    }

    // UI
    private RecyclerView rvMessages;
    private EditText     etMessage;
    private MaterialButton btnSend;   // במקום ImageButton
    private ProgressBar  progressBar;
    private TextView     tvNoMessages;

    // Data
    private final List<ChatMessage> messages = new ArrayList<>();
    private MessageAdapter          adapter;

    // Firebase
    private FirebaseFirestore db;
    private FirebaseAuth      auth;
    private ListenerRegistration chatMessagesListener; // New: ListenerRegistration for Firestore

    private String currentUid;
    private String chatId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (getArguments() != null) {
            chatId = getArguments().getString("chatId");
        }
        if (currentUid == null || chatId == null) {
            Toast.makeText(requireContext(),
                    "Error – user not signed in or chatId missing", Toast.LENGTH_SHORT).show();
            requireActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_chat, container, false);
        Log.d("ChatDebug", "inflated layout id = " + v.getId());
        rvMessages  = v.findViewById(R.id.rvMessages);
        etMessage   = v.findViewById(R.id.etMessage);
        btnSend     = v.findViewById(R.id.btnSend);
        Log.d("ChatDebug", "btnSend = " + btnSend);
        btnSend.setOnClickListener(view -> {
            Log.d("ChatDebug", "CLICK");
            sendMessage();
        });
        progressBar = v.findViewById(R.id.progressBar);
        tvNoMessages = v.findViewById(R.id.tvNoMessages);

        adapter = new MessageAdapter(messages, currentUid);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMessages.setAdapter(adapter);
        ((LinearLayoutManager) rvMessages.getLayoutManager())
                .setStackFromEnd(true);

        listenForMessages();
        return v;
    }

    private void listenForMessages() {
        showLoading(true);

        chatMessagesListener = db.collection("chats") // Assign to listenerRegistration
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    showLoading(false);

                    if (err != null) {
                        Log.e(TAG, "listen error", err);
                        Toast.makeText(requireContext(),
                                "Error loading messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snap == null) return;

                    messages.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        ChatMessage m = new ChatMessage();
                        m.setMessage(d.getString("message"));

                        // תומך גם בשדות ישנים "senderId" וגם ב-"userId"
                        String uid = d.getString("userId");
                        if (uid == null) uid = d.getString("senderId");
                        m.setSenderId(uid);

                        Long ts = d.getLong("timestamp");
                        m.setTimestamp(ts != null ? ts : 0L);

                        messages.add(m);
                    }
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    if (!messages.isEmpty()) {
                        rvMessages.scrollToPosition(messages.size() - 1);
                    }
                });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");

        ChatMessage m = new ChatMessage();
        m.setMessage(text);
        m.setSenderId(currentUid);
        long currentTimestamp = new Date().getTime(); // Get current time once
        m.setTimestamp(currentTimestamp);

        Log.d(TAG, "Sending message with timestamp: " + currentTimestamp); // New log

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(m)
                .addOnSuccessListener(documentReference -> {
                    // Update the parent chat document with the last message and time
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("lastMessage", m.getMessage());
                    updates.put("lastMessageTime", m.getTimestamp());
                    Log.d(TAG, "Updating chat document with last message time: " + m.getTimestamp()); // New log
                    db.collection("chats").document(chatId).update(updates)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Chat document updated with last message."))
                            .addOnFailureListener(e -> Log.e(TAG, "Error updating last message in chat document", e));
                })
                .addOnFailureListener(e -> {
                    showError("Send failed: " + e.getMessage());
                    Log.e(TAG, "sendMessage: ", e);
                });
    }

    // helpers
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(show ? View.GONE    : View.VISIBLE);
    }
    private void showError(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void updateEmptyState() {
        boolean empty = messages.isEmpty();
        tvNoMessages.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatMessagesListener != null) {
            chatMessagesListener.remove(); // Stop listening for updates
        }
    }
}
