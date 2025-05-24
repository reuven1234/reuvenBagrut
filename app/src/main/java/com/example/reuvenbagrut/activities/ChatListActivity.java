// File: app/src/main/java/com/example/reuvenbagrut/activities/ChatListActivity.java
package com.example.reuvenbagrut.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.reuvenbagrut.R;
import com.example.reuvenbagrut.adapters.ChatListAdapter;
import com.example.reuvenbagrut.models.ChatSummary;

import java.util.ArrayList;
import java.util.List;

public class ChatListActivity extends AppCompatActivity
        implements ChatListAdapter.OnChatClickListener {

    private RecyclerView      rvChats;
    private ChatListAdapter   adapter;
    private List<ChatSummary> chats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        rvChats  = findViewById(R.id.rvChats);
        adapter  = new ChatListAdapter(this, chats, this);
        rvChats.setLayoutManager(new LinearLayoutManager(this));
        rvChats.setAdapter(adapter);

        // TODO: load your list of ChatSummary into `chats` and then:
        // adapter.notifyDataSetChanged();
    }

    @Override
    public void onChatClick(String chatId) {
        Intent i = new Intent(this, ChatActivity.class);
        i.putExtra("CHAT_ID", chatId);
        startActivity(i);
    }
}
