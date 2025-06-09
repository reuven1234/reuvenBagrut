package com.example.reuvenbagrut.activities;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.reuvenbagrut.fragments.ChatFragment;
import com.example.reuvenbagrut.R;

public class ChatActivity extends AppCompatActivity {
    private String chatId;
    private String otherUserId;
    private String otherUserName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get data from intent
        chatId = getIntent().getStringExtra("chatId");
        otherUserId = getIntent().getStringExtra("otherUserId");
        otherUserName = getIntent().getStringExtra("otherUserName");

        if (chatId == null || otherUserId == null) {
            finish();
            return;
        }

        // Add ChatFragment
        if (savedInstanceState == null) {
            ChatFragment fragment = ChatFragment.newInstance(chatId, otherUserId, otherUserName);
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
        }
    }
}
