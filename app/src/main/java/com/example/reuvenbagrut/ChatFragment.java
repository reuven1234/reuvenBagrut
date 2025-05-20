package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.reuvenbagrut.adapters.ChatListAdapter;
import com.example.reuvenbagrut.models.Chat;
import com.example.reuvenbagrut.models.ChatMessage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatListAdapter.OnChatClickListener {
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<ChatMessage> messages;
    private String currentUserId;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize Firebase
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView);
        messages = new ArrayList<>();
        adapter = new ChatListAdapter(requireContext(), messages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Load chats
        loadChats();

        return view;
    }

    private void loadChats() {
        db.collection("chats")
            .whereArrayContains("participants", currentUserId)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener((value, error) -> {
                if (error != null) {
                    Toast.makeText(requireContext(), "Error loading chats", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (value != null) {
                    messages.clear();
                    for (var doc : value.getDocuments()) {
                        Chat chat = doc.toObject(Chat.class);
                        if (chat != null) {
                            chat.setId(doc.getId());
                            String otherUserId = chat.getOtherParticipantId(currentUserId);
                            if (otherUserId != null) {
                                // Create a ChatMessage from the last message info
                                ChatMessage message = new ChatMessage(
                                    chat.getLastMessageSenderId(),
                                    chat.getLastMessageSenderName(),
                                    chat.getLastMessageSenderImage(),
                                    chat.getLastMessage()
                                );
                                message.setTimestamp(chat.getLastMessageTime());
                                message.setId(chat.getId());
                                messages.add(message);
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public void onChatClick(ChatMessage message) {
        Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
        intent.putExtra("userId", message.getSenderId());
        startActivity(intent);
    }
} 