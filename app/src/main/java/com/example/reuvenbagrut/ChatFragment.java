// File: app/src/main/java/com/example/reuvenbagrut/ChatFragment.java
package com.example.reuvenbagrut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.adapters.ChatAdapter;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {
    private RecyclerView      rvMessages;
    private ChatAdapter       adapter;
    private List<ChatMessage> messages   = new ArrayList<>();
    private FirebaseFirestore db         = FirebaseFirestore.getInstance();
    private String            chatId;
    private String            currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        // must match the ID in fragment_chat.xml
        rvMessages = v.findViewById(R.id.rvMessages);

        adapter    = new ChatAdapter(messages, currentUid);
        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvMessages.setAdapter(adapter);

        if (getArguments() != null) {
            chatId = getArguments().getString("CHAT_ID");
        }
        if (chatId == null) return;

        db.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (snap == null) return;
                    messages.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ChatMessage msg = doc.toObject(ChatMessage.class);
                        if (msg != null) messages.add(msg);
                    }
                    adapter.notifyDataSetChanged();
                    rvMessages.scrollToPosition(messages.size() - 1);
                });
    }
}
