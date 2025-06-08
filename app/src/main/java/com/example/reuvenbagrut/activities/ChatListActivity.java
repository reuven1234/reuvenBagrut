// File: app/src/main/java/com/example/reuvenbagrut/activities/ChatListActivity.java
package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatListAdapter;
import com.example.reuvenbagrut.models.ChatSummary;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity implements ChatListAdapter.OnChatClickListener {
    private static final String TAG = "ChatListActivity";
    
    private RecyclerView rvChats;
    private ChatListAdapter adapter;
    private List<ChatSummary> chats;
    private ProgressBar progressBar;
    private TextView tvNoChats;
    private MaterialToolbar toolbar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
        
        if (currentUserId == null) {
            Toast.makeText(this, "Please sign in to view chats", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize views
        rvChats = findViewById(R.id.rvChats);
        progressBar = findViewById(R.id.progressBar);
        tvNoChats = findViewById(R.id.tvNoChats);
        toolbar = findViewById(R.id.toolbar);
        
        // Setup toolbar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Messages");
        
        // Setup RecyclerView
        chats = new ArrayList<>();
        adapter = new ChatListAdapter(this, chats, this);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(adapter);
        
        // Load chats
        loadChats();
    }
    
    private void loadChats() {
        showLoading(true);
        
        db.collection("chats")
          .whereArrayContains("participants", currentUserId)
          .orderBy("lastMessageTime", Query.Direction.DESCENDING)
          .get()
          .addOnCompleteListener(task -> {
              showLoading(false);
              
              if (task.isSuccessful()) {
                  chats.clear();
                  
                  for (QueryDocumentSnapshot document : task.getResult()) {
                      try {
                          ChatSummary chat = new ChatSummary();
                          chat.setId(document.getId());
                          chat.setLastMessage(document.getString("lastMessage"));
                          chat.setLastMessageTime(document.getLong("lastMessageTime"));
                          
                          // Get other participant's ID
                          List<String> participants = (List<String>) document.get("participants");
                          if (participants != null) {
                              for (String participantId : participants) {
                                  if (!participantId.equals(currentUserId)) {
                                      chat.setOtherUserId(participantId);
                                      break;
                                  }
                              }
                          }
                          
                          // Get other participant's name
                          if (chat.getOtherUserId() != null) {
                              db.collection("users").document(chat.getOtherUserId())
                                .get()
                                .addOnSuccessListener(userDoc -> {
                                    if (userDoc.exists()) {
                                        chat.setOtherUserName(userDoc.getString("name"));
                                        chat.setOtherUserImage(userDoc.getString("profileImage"));
                                        adapter.notifyDataSetChanged();
                                    }
                                });
                          }
                          
                          chats.add(chat);
                      } catch (Exception e) {
                          Log.e(TAG, "Error parsing chat: " + e.getMessage());
                      }
                  }
                  
                  adapter.notifyDataSetChanged();
                  updateEmptyState();
              } else {
                  showError("Error loading chats");
              }
          })
          .addOnFailureListener(e -> {
              showLoading(false);
              showError("Error loading chats");
          });
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        rvChats.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
    
    private void updateEmptyState() {
        tvNoChats.setVisibility(chats.isEmpty() ? View.VISIBLE : View.GONE);
        rvChats.setVisibility(chats.isEmpty() ? View.GONE : View.VISIBLE);
    }
    
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onChatClick(ChatSummary chat) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chat.getId());
        intent.putExtra("otherUserId", chat.getOtherUserId());
        intent.putExtra("otherUserName", chat.getOtherUserName());
        startActivity(intent);
    }
}
