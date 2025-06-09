package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatMessageAdapter;
import com.example.reuvenbagrut.models.ChatPreview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/** Activity המציגה את כל הצ’אטים של המשתמש המחובר. */
public class ChatListActivity extends AppCompatActivity {

    private RecyclerView       rvChats;
    private ProgressBar        progress;
    private TextView           tvEmpty;
    private ChatMessageAdapter adapter;
    private final List<ChatPreview> chats = new ArrayList<>();

    private FirebaseFirestore db;
    private String            currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChats  = findViewById(R.id.rvChats);
        progress = findViewById(R.id.progressBar);
        tvEmpty  = findViewById(R.id.tvEmpty);

        adapter = new ChatMessageAdapter(
                chats,
                chat -> {
                    Intent i = new Intent(ChatListActivity.this, ChatActivity.class);
                    i.putExtra("otherUid",  chat.getOtherUid());
                    i.putExtra("otherName", chat.getOtherName());
                    startActivity(i);
                }
        );

        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(adapter);

        db         = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChats();
    }

    private void loadChats() {
        progress.setVisibility(View.VISIBLE);

        db.collection("chats")
                .whereArrayContains("participants", currentUid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    chats.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        String otherUid = null;
                        List<String> parts = (List<String>) d.get("participants");
                        if (parts != null)
                            for (String p : parts) if (!p.equals(currentUid)) otherUid = p;
                        if (otherUid == null) continue;

                        ChatPreview cp = new ChatPreview();
                        cp.setOtherUid(otherUid);
                        cp.setOtherName(d.getString("otherName"));
                        cp.setLastMessage(d.getString("lastMessage"));
                        Long ts = d.getLong("lastMessageTime");
                        cp.setLastMessageTime(ts != null ? ts : 0L);

                        chats.add(cp);
                    }
                    adapter.notifyDataSetChanged();
                    progress.setVisibility(View.GONE);
                    tvEmpty.setVisibility(chats.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> progress.setVisibility(View.GONE));
    }
}
