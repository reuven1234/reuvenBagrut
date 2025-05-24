package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.widget.EditText;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView      recyclerView;
    private EditText          edtMessage;
    private ChatAdapter       adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private FirebaseFirestore db;
    private FirebaseUser      currentUser;
    private String            chatId = "CHAT_ID_HERE"; // TODO: replace

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.recyclerChat);
        edtMessage   = findViewById(R.id.etMessage);

        db          = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // 1) Setup RecyclerView + Adapter
        adapter = new ChatAdapter(messages, currentUser.getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 2) Listen for new messages
        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null) return;
                    messages.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        messages.add(ChatMessage.fromDocument(doc));
                    }
                    adapter.notifyDataSetChanged();
                    // scroll to bottom
                    recyclerView.scrollToPosition(messages.size() - 1);
                });

        // 3) Send button
        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (text.isEmpty()) return;
            ChatMessage msg = new ChatMessage(
                    null,
                    currentUser.getUid(),
                    text,
                    System.currentTimeMillis(),
                    false
            );
            db.collection("chats")
                    .document(chatId)
                    .collection("messages")
                    .add(msg);
            edtMessage.setText("");
        });
    }
}
