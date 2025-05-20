package com.example.reuvenbagrut;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.reuvenbagrut.adapters.ProfileTabsAdapter;
import com.example.reuvenbagrut.models.User;
import com.example.reuvenbagrut.activities.EditProfileActivity;

public class ProfileFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView recipesCountText;
    private TextView followersCountText;
    private TextView followingCountText;
    private TextView bioText;
    private TextView editProfileButton;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        
        initializeViews(view);
        setupViewPager();
        loadUserData();
        
        return view;
    }

    private void initializeViews(View view) {
        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tabLayout);
        recipesCountText = view.findViewById(R.id.recipesCountText);
        followersCountText = view.findViewById(R.id.followersCountText);
        followingCountText = view.findViewById(R.id.followingCountText);
        bioText = view.findViewById(R.id.bioText);
        editProfileButton = view.findViewById(R.id.editProfileButton);

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupViewPager() {
        ProfileTabsAdapter adapter = new ProfileTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> {
                switch (position) {
                    case 0:
                        tab.setText(R.string.uploaded_recipes);
                        break;
                    case 1:
                        tab.setText(R.string.liked_recipes);
                        break;
                }
            }).attach();
    }

    private void loadUserData() {
        if (currentUser == null) return;

        db.collection("users")
          .document(currentUser.getUid())
          .get()
          .addOnSuccessListener(documentSnapshot -> {
              User user = documentSnapshot.toObject(User.class);
              if (user != null) {
                  bioText.setText(user.getBio());
              }
          });

        // Load recipes count
        db.collection("recipes")
          .whereEqualTo("userId", currentUser.getUid())
          .get()
          .addOnSuccessListener(querySnapshot -> {
              recipesCountText.setText(String.valueOf(querySnapshot.size()));
          });

        // Load followers count
        db.collection("users")
          .document(currentUser.getUid())
          .collection("followers")
          .get()
          .addOnSuccessListener(querySnapshot -> {
              followersCountText.setText(String.valueOf(querySnapshot.size()));
          });

        // Load following count
        db.collection("users")
          .document(currentUser.getUid())
          .collection("following")
          .get()
          .addOnSuccessListener(querySnapshot -> {
              followingCountText.setText(String.valueOf(querySnapshot.size()));
          });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}
