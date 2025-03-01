package com.example.reuvenbagrut;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile_nav_Fragment extends Fragment {

    private FirebaseUser user;
    private TextView hiTxt, bioTxt;
    private de.hdodenhof.circleimageview.CircleImageView profileImageView;
    private Button editProfileBtn;
    private ImageButton settings;

    // TabLayout and ViewPager2 for switching between recipe sections
    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public Profile_nav_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             android.os.Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_nav_, container, false);

        // Bind header views
        settings = view.findViewById(R.id.settings);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);
        hiTxt = view.findViewById(R.id.username);
        bioTxt = view.findViewById(R.id.bio);
        profileImageView = view.findViewById(R.id.profileImage);

        // Bind TabLayout and ViewPager2
        tabLayout = view.findViewById(R.id.tabs);
        viewPager = view.findViewById(R.id.viewPager);

        user = FirebaseAuth.getInstance().getCurrentUser();

        // Set up the ViewPager with the adapter that returns two fragments.
        ProfileTabsAdapter adapter = new ProfileTabsAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach the TabLayout with ViewPager2
        tabLayout.addTab(tabLayout.newTab().setText("Uploaded"));
        tabLayout.addTab(tabLayout.newTab().setText("Liked"));

        // Sync TabLayout selection with ViewPager page changes
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (tabLayout.getSelectedTabPosition() != position) {
                    tabLayout.selectTab(tabLayout.getTabAt(position));
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Set click listeners
        settings.setOnClickListener(v -> {
            Settings_nav_Fragment fragment1 = new Settings_nav_Fragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.flFragment, fragment1);
            transaction.commit();
        });

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        refreshUserData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUserData();
    }

    private void refreshUserData() {
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String bio = documentSnapshot.getString("bio");
                            String imageBase64 = documentSnapshot.getString("profilePicture");

                            hiTxt.setText((name != null && !name.isEmpty()) ? name : user.getDisplayName());
                            if (bio != null && !bio.isEmpty()) {
                                bioTxt.setText(bio);
                            }
                            if (imageBase64 != null && !imageBase64.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    profileImageView.setImageBitmap(decodedBitmap);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }
}
