package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class ChatDetailActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText edtMessage;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private String chatId = "CHAT_ID_HERE"; // Replace with actual

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        recyclerView = findViewById(R.id.rvChatDetail);
        edtMessage = findViewById(R.id.etMessageDetail);

        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("chats").document(chatId).collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    // update adapter
                });

        findViewById(R.id.btnSendDetail).setOnClickListener(v -> {
            String message = edtMessage.getText().toString();
            if (!message.isEmpty()) {
                ChatMessage msg = new ChatMessage(
                        null,
                        currentUser.getUid(),
                        message,
                        System.currentTimeMillis(),
                        false
                );
                db.collection("chats").document(chatId)
                        .collection("messages").add(msg);
                edtMessage.setText("");
            }
        });
    }
}
