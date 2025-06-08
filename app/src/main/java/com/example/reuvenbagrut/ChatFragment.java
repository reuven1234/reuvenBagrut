// File: app/src/main/java/com/example/reuvenbagrut/ChatFragment.java
package com.example.reuvenbagrut;

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

import com.example.reuvenbagrut.adapters.MessageAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatFragment extends Fragment {
    private static final String TAG = "ChatFragment";
    
    private RecyclerView rvMessages;
    private MessageAdapter adapter;
    private List<ChatMessage> messages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ProgressBar progressBar;
    private TextView tvNoMessages;
    
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String chatId;
    private String currentUserId;
    private String otherUserId;
    
    public static ChatFragment newInstance(String chatId, String otherUserId) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        args.putString("otherUserId", otherUserId);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (getArguments() != null) {
            chatId = getArguments().getString("chatId");
            otherUserId = getArguments().getString("otherUserId");
        }
        
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        
        if (currentUserId == null || chatId == null) {
            Toast.makeText(getContext(), "Error: User not authenticated or chat ID missing", Toast.LENGTH_SHORT).show();
            getActivity().finish();
            return;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        
        // Initialize views
        rvMessages = view.findViewById(R.id.rvMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoMessages = view.findViewById(R.id.tvNoMessages);
        
        // Defensive: check chatId
        if (chatId == null) {
            Toast.makeText(getContext(), "Error: Chat ID is missing.", Toast.LENGTH_LONG).show();
            // Post the back press to avoid FragmentManager transaction issues
            if (getActivity() != null) {
                view.post(() -> {
                    if (getActivity() != null) {
                        getActivity().onBackPressed();
                    }
                });
            }
            return view;
        }
        
        // Setup RecyclerView
        messages = new ArrayList<>();
        adapter = new MessageAdapter(messages, currentUserId);
        rvMessages.setLayoutManager(new LinearLayoutManager(getContext()));
        rvMessages.setAdapter(adapter);
        
        // Setup send button
        btnSend.setOnClickListener(v -> sendMessage());
        
        // Load messages
        loadMessages();
        
        return view;
    }
    
    private void loadMessages() {
        showLoading(true);
        
        db.collection("chats")
          .document(chatId)
          .collection("messages")
          .orderBy("timestamp", Query.Direction.ASCENDING)
          .addSnapshotListener((value, error) -> {
              showLoading(false);
              
              if (error != null) {
                  showError("Error loading messages");
                  return;
              }
              
              if (value != null) {
                  messages.clear();
                  
                  for (var doc : value.getDocuments()) {
                      try {
                          ChatMessage message = new ChatMessage();
                          message.setId(doc.getId());
                          message.setMessage(doc.getString("message"));
                          message.setUserId(doc.getString("userId"));
                          message.setTimestamp(doc.getLong("timestamp"));
                          
                          messages.add(message);
                      } catch (Exception e) {
                          Log.e(TAG, "Error parsing message: " + e.getMessage());
                      }
                  }
                  
                  adapter.notifyDataSetChanged();
                  updateEmptyState();
                  
                  // Scroll to bottom
                  if (!messages.isEmpty()) {
                      rvMessages.smoothScrollToPosition(messages.size() - 1);
                  }
              }
          });
    }
    
    private void sendMessage() {
        String messageText = etMessage.getText().toString().trim();
        
        if (messageText.isEmpty()) {
            return;
        }
        
        // Clear input
        etMessage.setText("");
        
        // Create message
        ChatMessage message = new ChatMessage();
        message.setMessage(messageText);
        message.setUserId(currentUserId);
        message.setTimestamp(new Date().getTime());
        
        // Add to Firestore
        db.collection("chats")
          .document(chatId)
          .collection("messages")
          .add(message)
          .addOnSuccessListener(documentReference -> {
              // Update chat document with last message
              db.collection("chats")
                .document(chatId)
                .update(
                    "lastMessage", messageText,
                    "lastMessageTime", message.getTimestamp()
                );
          })
          .addOnFailureListener(e -> {
              showError("Error sending message");
              Log.e(TAG, "Error sending message: " + e.getMessage());
          });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateEmptyState() {
        tvNoMessages.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
        rvMessages.setVisibility(messages.isEmpty() ? View.GONE : View.VISIBLE);
    }
}

