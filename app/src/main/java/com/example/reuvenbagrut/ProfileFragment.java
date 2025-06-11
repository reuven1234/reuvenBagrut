package com.example.reuvenbagrut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.reuvenbagrut.adapters.ProfileTabsAdapter;
import com.example.reuvenbagrut.models.User;
import com.example.reuvenbagrut.activities.EditProfileActivity;

public class ProfileFragment extends Fragment {
    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private TextView recipesCountText;
    private TextView bioText;
    private TextView editProfileButton;
    private TextView usernameText;
    private ShapeableImageView profileImage;
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
        bioText = view.findViewById(R.id.bioText);
        editProfileButton = view.findViewById(R.id.editProfileButton);
        usernameText = view.findViewById(R.id.usernameText);
        profileImage = view.findViewById(R.id.profileImage);

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
                        usernameText.setText(user.getName());

                        String profileImageUrl = user.getPhotoUrl();
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(profileImageUrl, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(this)
                                    .load(decodedByte)
                                    .circleCrop()
                                    .into(profileImage);
                            } catch (IllegalArgumentException e) {
                                // Handle invalid Base64 string if necessary
                                e.printStackTrace();
                            }
                        } else {
                            // Load default placeholder if no image URL exists
                            Glide.with(this)
                                .load(R.drawable.ic_profile_placeholder)
                                .circleCrop()
                                .into(profileImage);
                        }
                    }
                });

        // Load recipes count
        db.collection("recipes")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    recipesCountText.setText(String.valueOf(querySnapshot.size()));
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }
}