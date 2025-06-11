package com.example.reuvenbagrut.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.activities.ChatActivity;
import com.example.reuvenbagrut.adapters.ChatMessageAdapter;
import com.example.reuvenbagrut.models.ChatPreview;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.List;

import com.example.reuvenbagrut.models.User;

public class ChatListFragment extends Fragment {

    private RecyclerView rvChats;
    private ChatMessageAdapter adapter;
    private final List<ChatPreview> chats = new ArrayList<>();

    private FirebaseFirestore db;
    private String currentUid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        rvChats = v.findViewById(R.id.rvChats);
        rvChats.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new ChatMessageAdapter(chats, chat ->
                startActivity(new Intent(requireContext(), ChatActivity.class)
                        .putExtra("chatId",        chat.getDocumentId())
                        .putExtra("otherUserId",   chat.getOtherUid())
                        .putExtra("otherUserName", chat.getOtherName())
                        .putExtra("otherUserProfileImage", chat.getProfileImageUrl())));
        rvChats.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        currentUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        loadChats();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh chats every time the fragment is resumed
        loadChats();
    }

    private void loadChats() {
        if (currentUid == null) {
            Log.e("ChatListFragment", "Current user ID is null.");
            return;
        }

        db.collection("chats")
                .whereArrayContains("participants", currentUid)
                .orderBy("lastMessageTime", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(chatSnapshots -> {
                    chats.clear();
                    List<Task<Void>> userFetchTasks = new ArrayList<>();

                    for (DocumentSnapshot chatDoc : chatSnapshots.getDocuments()) {
                        List<String> participants = (List<String>) chatDoc.get("participants");
                        if (participants == null || participants.size() < 2) {
                            continue;
                        }
                        String otherUid = participants.get(0).equals(currentUid) ? participants.get(1) : participants.get(0);

                        String lastMessage = chatDoc.getString("lastMessage");
                        Long lastMessageTime = chatDoc.getLong("lastMessageTime");

                        // Create a temporary ChatPreview object with basic info
                        ChatPreview tempChatPreview = new ChatPreview(
                                otherUid,
                                "Loading...", // Placeholder name
                                (lastMessage != null) ? lastMessage : "",
                                (lastMessageTime != null) ? lastMessageTime : 0L,
                                null // Placeholder image URL
                        );
                        tempChatPreview.setDocumentId(chatDoc.getId());
                        chats.add(tempChatPreview); // Add to list immediately for display

                        // Fetch other user's details (name and profile image)
                        Task<Void> userTask = db.collection("users").document(otherUid).get()
                                .continueWithTask(task -> {
                                    if (task.isSuccessful()) {
                                        DocumentSnapshot userDoc = task.getResult();
                                        String fetchedUserName = userDoc.getString("name");
                                        String fetchedProfileImageUrl = userDoc.getString("profileImageUrl");

                                        // Update the corresponding ChatPreview object in the list
                                        for (ChatPreview cp : chats) {
                                            if (cp.getOtherUid().equals(otherUid)) {
                                                cp.setOtherName(fetchedUserName != null ? fetchedUserName : "Unknown");
                                                cp.setProfileImageUrl(fetchedProfileImageUrl);
                                                break;
                                            }
                                        }
                                        return Tasks.forResult(null); // Return a completed task on success
                                    } else {
                                        Log.e("ChatListFragment", "Error fetching user details for " + otherUid + ": " + task.getException().getMessage());
                                        return Tasks.forResult(null); // Return a completed task even on failure to not break whenAllSuccess
                                    }
                                });

                        userFetchTasks.add(userTask);
                    }

                    // Once all user details are fetched, update the adapter
                    Tasks.whenAllSuccess(userFetchTasks)
                            .addOnSuccessListener(results -> {
                                adapter.notifyDataSetChanged();
                                Log.d("ChatListFragment", "All user details fetched and adapter updated.");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ChatListFragment", "Error fetching all user details: " + e.getMessage());
                                adapter.notifyDataSetChanged(); // Still update with available data
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ChatListFragment", "Error loading chats: " + e.getMessage());
                });
    }
} 