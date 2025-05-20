package com.example.reuvenbagrut;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.reuvenbagrut.adapters.ChatAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.example.reuvenbagrut.models.Chat;
import com.example.reuvenbagrut.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private ImageView profileImage;
    private TextView userName;
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messages;
    private String currentUserId;
    private String otherUserId;
    private String chatId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        // Initialize views
        profileImage = findViewById(R.id.profileImage);
        userName = findViewById(R.id.userName);
        recyclerView = findViewById(R.id.recyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Get other user's ID from intent
        otherUserId = getIntent().getStringExtra("userId");
        if (otherUserId == null) {
            Toast.makeText(this, "Error: User ID not provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chat ID (combination of both user IDs)
        chatId = generateChatId(currentUserId, otherUserId);

        // Load other user's profile
        loadUserProfile();

        // Setup RecyclerView
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, messages, currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        // Create or load chat
        createOrLoadChat();

        // Setup send button
        sendButton.setOnClickListener(v -> sendMessage());
    }

    private String generateChatId(String userId1, String userId2) {
        // Sort IDs to ensure consistent chat ID regardless of who initiates
        return userId1.compareTo(userId2) < 0 ? 
            userId1 + "_" + userId2 : 
            userId2 + "_" + userId1;
    }

    private void loadUserProfile() {
        db.collection("users").document(otherUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    userName.setText(user.getDisplayName());
                    if (user.getPhotoUrl() != null && !user.getPhotoUrl().isEmpty()) {
                        Glide.with(this)
                            .load(user.getPhotoUrl())
                            .circleCrop()
                            .into(profileImage);
                    }
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading user profile", Toast.LENGTH_SHORT).show());
    }

    private void createOrLoadChat() {
        db.collection("chats").document(chatId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                    // Create new chat
                    List<String> participants = new ArrayList<>();
                    participants.add(currentUserId);
                    participants.add(otherUserId);
                    
                    Chat chat = new Chat(participants);
                    db.collection("chats").document(chatId)
                        .set(chat)
                        .addOnSuccessListener(aVoid -> loadMessages())
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Error creating chat", Toast.LENGTH_SHORT).show());
                } else {
                    loadMessages();
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(this, "Error loading messages", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    messages.clear();
                    for (var doc : value.getDocuments()) {
                        ChatMessage message = doc.toObject(ChatMessage.class);
                        if (message != null) {
                            message.setId(doc.getId());
                            messages.add(message);
                        }
                    }
                    chatAdapter.notifyDataSetChanged();
                    recyclerView.scrollToPosition(messages.size() - 1);
                }
            });
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) return;

        // Get current user's name and photo URL
        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                User currentUser = documentSnapshot.toObject(User.class);
                if (currentUser != null) {
                    ChatMessage message = new ChatMessage(
                        currentUserId,
                        currentUser.getDisplayName(),
                        currentUser.getPhotoUrl(),
                        messageText
                    );

                    // Save message to Firestore
                    db.collection("chats")
                        .document(chatId)
                        .collection("messages")
                        .add(message)
                        .addOnSuccessListener(documentReference -> {
                            messageInput.setText("");
                            message.setId(documentReference.getId());
                        })
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Error sending message", Toast.LENGTH_SHORT).show());
                }
            });
    }
} 