package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;

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

/** מסך רשימת-הצ’אטים שה-MainActivity מפעיל. */
public class ChatListActivity extends AppCompatActivity {

    private RecyclerView       rvChats;
    private ChatMessageAdapter adapter;
    private final List<ChatPreview> chats = new ArrayList<>();

    private FirebaseFirestore db;
    private String            currentUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChats  = findViewById(R.id.rvChats);
        adapter = new ChatMessageAdapter(
                chats,
                chat -> startActivity(new Intent(this, ChatActivity.class)
                        /* ↓ ↓ ↓  שולחים ישירות את chatId שהאדפטר מחזיק */
                        .putExtra("chatId",        chat.getDocumentId())
                        .putExtra("otherUserId",   chat.getOtherUid())
                        .putExtra("otherUserName", chat.getOtherName())));

        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(adapter);

        db         = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChats();
    }

    private void loadChats() {
        db.collection("chats")
                .whereArrayContains("participants", currentUid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    chats.clear();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        List<String> parts = (List<String>) d.get("participants");
                        if (parts == null) continue;
                        String other = parts.get(0).equals(currentUid) ? parts.get(1) : parts.get(0);

                        /* -------- צור ChatPreview עם 4 פרמטרים -------- */
                        ChatPreview p = new ChatPreview(
                                other,                                 // otherUid
                                d.getString("otherName"),              // otherName
                                d.getString("lastMessage"),            // lastMessage
                                d.getLong("lastMessageTime") != null   // lastMessageTime
                                        ? d.getLong("lastMessageTime") : 0L
                        );
                        /* נשמור גם את ה-document id כדי להשתמש בו בה Intent */
                        p.setDocumentId(d.getId());                   // ← צריך setter (ראה למטה)
                        chats.add(p);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
