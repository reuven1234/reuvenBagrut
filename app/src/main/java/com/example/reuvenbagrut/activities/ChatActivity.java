package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;

    private String otherUserId;
    private String otherUserName;
    private String otherUserImage;
    private String currentUserId;
    private String chatId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Intent extras
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        otherUserImage = getIntent().getStringExtra("otherUserImage");

        if (otherUserId == null) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Generate chat ID (combination of both user IDs)
        chatId = generateChatId(currentUserId, otherUserId);

        // Setup send button
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());

        // Load messages
        loadMessages();
    }

    private String generateChatId(String userId1, String userId2) {
        // Sort the IDs to ensure consistent chat ID regardless of who initiates
        return userId1.compareTo(userId2) < 0 ? 
            userId1 + "_" + userId2 : 
            userId2 + "_" + userId1;
    }

    private void loadMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (snapshots != null) {
                    messages.clear();
                    for (var doc : snapshots.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            messages.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        recyclerView.smoothScrollToPosition(messages.size() - 1);
                    }
                }
            });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        Map<String, Object> message = new HashMap<>();
        message.put("text", messageText);
        message.put("senderId", currentUserId);
        message.put("senderName", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
        message.put("senderImage", FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl() != null ? 
            FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl().toString() : null);
        message.put("timestamp", System.currentTimeMillis());

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                messageInput.setText("");
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
            });
    }
} 