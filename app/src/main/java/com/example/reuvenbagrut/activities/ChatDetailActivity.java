package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatAdapter;
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
    private String currentUserId;
    private String currentUserName;
    private String currentUserImage;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get Intent extras
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        if (chatId == null || otherUserId == null) {
            Toast.makeText(this,
                    "Error: chat data missing",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // View binding
        toolbar      = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        progressBar  = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        loadOtherUserData();
        loadCurrentUserData();

        messages    = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        loadMessages();

        findViewById(R.id.sendButton).setOnClickListener(v -> sendMessage());
    }

    private void loadOtherUserData() {
        db.collection("users")
                .document(otherUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    User other = doc.toObject(User.class);
                    if (other != null) {
                        toolbar.setTitle(other.getName());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error loading user data",
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void loadCurrentUserData() {
        db.collection("users")
                .document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User current = documentSnapshot.toObject(User.class);
                    if (current != null) {
                        currentUserName  = current.getName();
                        currentUserImage = current.getImageUrl();
                    }
                })
                .addOnFailureListener(e -> {
                    // Name/image may remain null, but sending still works
                });
    }

    private void loadMessages() {
        progressBar.setVisibility(CircularProgressIndicator.VISIBLE);
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) {
                        Toast.makeText(this,
                                "Error loading messages",
                                Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(CircularProgressIndicator.GONE);
                        return;
                    }
                    if (snap != null) {
                        messages.clear();
                        for (DocumentSnapshot d : snap.getDocuments()) {
                            ChatMessage msg = d.toObject(ChatMessage.class);
                            if (msg != null) {
                                messages.add(msg);
                            }
                        }
                        chatAdapter.notifyDataSetChanged();
                        if (!messages.isEmpty()) {
                            recyclerView.smoothScrollToPosition(messages.size() - 1);
                        }
                    }
                    progressBar.setVisibility(CircularProgressIndicator.GONE);
                });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        ChatMessage msg = new ChatMessage(
                currentUserId,
                currentUserName,
                currentUserImage,
                text
        );

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .add(msg)
                .addOnSuccessListener(ref -> {
                    messageInput.setText("");
                    // Update chat summary
                    db.collection("chats")
                            .document(chatId)
                            .update(
                                    "lastMessage", text,
                                    "lastMessageTime", msg.getTimestamp(),
                                    "lastMessageSenderId", currentUserId,
                                    "lastMessageSenderName", currentUserName,
                                    "lastMessageSenderImage", currentUserImage
                            );
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Error sending message",
                            Toast.LENGTH_SHORT).show();
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
