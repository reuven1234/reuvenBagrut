package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.MessageAdapter;
import com.example.reuvenbagrut.models.Message;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView messagesRecyclerView;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private String otherUserId;
    private String otherUserName;
    private String chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get user info from intent
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");
        if (otherUserId == null || otherUserName == null) {
            Toast.makeText(this, "Error: Invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        messagesRecyclerView = findViewById(R.id.messagesRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Setup toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // Initialize Firestore and current user
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login to chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chat ID (sorted to ensure consistency)
        chatId = currentUser.getUid().compareTo(otherUserId) < 0 ?
                currentUser.getUid() + "_" + otherUserId :
                otherUserId + "_" + currentUser.getUid();

        // Initialize RecyclerView
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages, currentUser.getUid());
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        messagesRecyclerView.setAdapter(messageAdapter);

        // Load messages
        loadMessages();

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private void loadMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((snapshots, error) -> {
                if (error != null || snapshots == null) return;
                
                messages.clear();
                for (var doc : snapshots.getDocuments()) {
                    Message message = doc.toObject(Message.class);
                    if (message != null) {
                        message.setId(doc.getId());
                        messages.add(message);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                if (!messages.isEmpty()) {
                    messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
                }
            });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Message message = new Message();
        message.setSenderId(currentUser.getUid());
        message.setSenderName(currentUser.getDisplayName());
        message.setContent(text);
        message.setTimestamp(System.currentTimeMillis());

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
            .addOnSuccessListener(documentReference -> {
                messageInput.setText("");
                // Update last message in chat metadata
                db.collection("chats")
                    .document(chatId)
                    .set(new ChatMetadata(
                        currentUser.getUid(),
                        otherUserId,
                        text,
                        System.currentTimeMillis()
                    ));
            })
            .addOnFailureListener(e ->
                Toast.makeText(ChatActivity.this,
                    "Failed to send message",
                    Toast.LENGTH_SHORT).show()
            );
    }

    private static class ChatMetadata {
        private String user1Id;
        private String user2Id;
        private String lastMessage;
        private long lastMessageTime;

        public ChatMetadata() {} // Required for Firestore

        public ChatMetadata(String user1Id, String user2Id, String lastMessage, long lastMessageTime) {
            this.user1Id = user1Id;
            this.user2Id = user2Id;
            this.lastMessage = lastMessage;
            this.lastMessageTime = lastMessageTime;
        }

        // Getters and setters
        public String getUser1Id() { return user1Id; }
        public void setUser1Id(String user1Id) { this.user1Id = user1Id; }
        public String getUser2Id() { return user2Id; }
        public void setUser2Id(String user2Id) { this.user2Id = user2Id; }
        public String getLastMessage() { return lastMessage; }
        public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
        public long getLastMessageTime() { return lastMessageTime; }
        public void setLastMessageTime(long lastMessageTime) { this.lastMessageTime = lastMessageTime; }
    }
} 