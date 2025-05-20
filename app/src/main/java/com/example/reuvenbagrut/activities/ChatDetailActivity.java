package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatAdapter;
import com.example.reuvenbagrut.models.Chat;
import com.example.reuvenbagrut.models.ChatMessage;
import com.example.reuvenbagrut.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;
    private CircularProgressIndicator progressBar;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private String chatId;
    private String otherUserId;
    private FirebaseFirestore db;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get chat data from intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");

        if (chatId == null) {
            Toast.makeText(this, "Error: Chat ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        progressBar = findViewById(R.id.progressBar);

        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Load other user's data
        loadOtherUserData();

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Load messages
        loadMessages();

        // Setup send button
        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());
    }

    private void loadOtherUserData() {
        db.collection("users").document(otherUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User otherUser = documentSnapshot.toObject(User.class);
                    if (otherUser != null) {
                        toolbar.setTitle(otherUser.getName());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadMessages() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        return;
                    }

                    messages.clear();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
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
                    progressBar.setVisibility(View.GONE);
                });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        ChatMessage message = new ChatMessage(
                currentUserId,
                currentUserId,
                currentUserId,
                messageText
        );

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    // Update last message in chat document
                    db.collection("chats").document(chatId)
                            .update("lastMessage", messageText,
                                    "lastMessageTimestamp", new Date());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
} 