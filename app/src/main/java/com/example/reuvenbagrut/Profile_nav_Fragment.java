package com.example.reuvenbagrut;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.imageview.ShapeableImageView;

public class Profile_nav_Fragment extends Fragment {

    private FirebaseUser user;
    private TextView hiTxt, bioTxt;
    private ShapeableImageView profileImageView;
    private Button editProfileBtn;
    private ImageButton settings;

    public Profile_nav_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_nav_, container, false);
        settings = view.findViewById(R.id.settings);
        editProfileBtn = view.findViewById(R.id.editProfileBtn);
        hiTxt = view.findViewById(R.id.username);
        bioTxt = view.findViewById(R.id.bio);
        profileImageView = view.findViewById(R.id.profileImage);
        user = FirebaseAuth.getInstance().getCurrentUser();

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


// ...

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
