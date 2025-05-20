package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.reuvenbagrut.activities.ChatDetailActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment implements ChatListAdapter.OnChatClickListener {
    private static final String TAG = "ChatFragment";
    private RecyclerView recyclerView;
    private ChatListAdapter adapter;
    private List<ChatMessage> messages;
    private String currentUserId;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view chats", Toast.LENGTH_SHORT).show();
            return view;
        }

        currentUserId = currentUser.getUid();
        db = FirebaseFirestore.getInstance();

        // RecyclerView setup
        recyclerView = view.findViewById(R.id.recyclerView);
        messages = new ArrayList<>();
        adapter = new ChatListAdapter(requireContext(), messages, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadChats();
        return view;
    }

    private void loadChats() {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view chats", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get all chats where the user is a participant
        db.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messages.clear();
                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        try {
                            Chat chat = doc.toObject(Chat.class);
                            if (chat == null) {
                                Log.w(TAG, "Chat object is null for document: " + doc.getId());
                                continue;
                            }
                            chat.setId(doc.getId());

                            // Get the other participant's ID
                            String otherUserId = chat.getOtherParticipantId(currentUserId);
                            if (otherUserId == null) {
                                Log.w(TAG, "Other participant ID is null for chat: " + chat.getId());
                                continue;
                            }

                            // Build a preview message object
                            ChatMessage preview = new ChatMessage(
                                    otherUserId,
                                    chat.getLastMessageSenderName(),
                                    chat.getLastMessageSenderImage(),
                                    chat.getLastMessage()
                            );
                            preview.setTimestamp(chat.getLastMessageTime());
                            preview.setId(chat.getId());
                            messages.add(preview);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing chat document: " + doc.getId(), e);
                        }
                    }
                    // Sort messages by timestamp in descending order
                    messages.sort((m1, m2) -> Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading chats", e);
                    if (e instanceof FirebaseFirestoreException) {
                        FirebaseFirestoreException firestoreError = (FirebaseFirestoreException) e;
                        Toast.makeText(requireContext(),
                                "Error loading chats: " + firestoreError.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(),
                                "Error loading chats. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onChatClick(ChatMessage message) {
        if (currentUser == null) {
            Toast.makeText(requireContext(), "Please sign in to view chat details", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(requireContext(), ChatDetailActivity.class);
        intent.putExtra("chatId", message.getId());
        intent.putExtra("otherUserId", message.getSenderId());
        startActivity(intent);
    }
}
