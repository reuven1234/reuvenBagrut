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
import android.widget.ProgressBar;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.Toast;
import java.util.List;

public class ProfileFragment extends Fragment {
    private static final String TAG = "ProfileFragment";
    private static final String KEY_SELECTED_TAB = "selected_tab";
    private static final int DEFAULT_TAB = 0;

    private FirebaseUser user;
    private TextView hiTxt;
    private TextView bioTxt;
    private de.hdodenhof.circleimageview.CircleImageView profileImageView;
    private Button editProfileBtn;
    private ImageButton settings;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProfileTabsAdapter tabsAdapter;
    private TabLayoutMediator tabLayoutMediator;
    private FloatingActionButton fabAddRecipe;
    private ProgressBar progressBar;
    
    // New social stats views
    private TextView recipesCountText;
    private TextView followersCountText;
    private TextView followingCountText;
    
    private int selectedTab = DEFAULT_TAB;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;
    private boolean isViewCreated = false;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB, DEFAULT_TAB);
        }
        
        // Initialize Firebase user
        initializeUser();
        
        // Initialize the page change callback
        initializePageChangeCallback();
    }

    private void initializeUser() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null && isAdded()) {
            navigateToLogin();
        }
    }

    private void initializePageChangeCallback() {
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (isAdded()) {
                    selectedTab = position;
                }
            }
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isViewCreated = true;
        initializeViews(view);
        setupViewPager();
        setupClickListeners();
    }

    private void initializeViews(View view) {
        try {
            settings = view.findViewById(R.id.settings);
            editProfileBtn = view.findViewById(R.id.editProfileBtn);
            hiTxt = view.findViewById(R.id.hiTxt);
            bioTxt = view.findViewById(R.id.bio);
            profileImageView = view.findViewById(R.id.profileImage);
            tabLayout = view.findViewById(R.id.tabs);
            viewPager = view.findViewById(R.id.viewPager);
            fabAddRecipe = view.findViewById(R.id.fabAddRecipe);
            progressBar = view.findViewById(R.id.progressBar);
            
            // Initialize social stats views
            recipesCountText = view.findViewById(R.id.recipesCountText);
            followersCountText = view.findViewById(R.id.followersCountText);
            followingCountText = view.findViewById(R.id.followingCountText);
            
            // Initialize with default values
            setDefaultStats();
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
        }
    }
    
    private void setDefaultStats() {
        if (recipesCountText != null) recipesCountText.setText("0");
        if (followersCountText != null) followersCountText.setText("0");
        if (followingCountText != null) followingCountText.setText("0");
    }

    private void setupViewPager() {
        if (!isAdded() || viewPager == null) return;

        try {
            // Create adapter if not exists
            if (tabsAdapter == null) {
                tabsAdapter = new ProfileTabsAdapter(this);
            }

            // Configure ViewPager2
            viewPager.setOffscreenPageLimit(1);
            viewPager.setUserInputEnabled(true);
            
            // Register the callback
            if (pageChangeCallback != null) {
                viewPager.registerOnPageChangeCallback(pageChangeCallback);
            }

            // Set adapter
            viewPager.setAdapter(tabsAdapter);
            
            // Setup TabLayoutMediator
            setupTabLayoutMediator();
            
            // Restore selected tab
            if (selectedTab >= 0 && selectedTab < tabsAdapter.getItemCount()) {
                viewPager.setCurrentItem(selectedTab, false);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting up ViewPager", e);
        }
    }

    private void setupTabLayoutMediator() {
        if (!isAdded() || tabLayout == null || viewPager == null) return;

        try {
            if (tabLayoutMediator != null) {
                tabLayoutMediator.detach();
            }
            
            tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    if (tabsAdapter != null) {
                        tab.setText(tabsAdapter.getTabTitle(position));
                    }
                }
            );
            tabLayoutMediator.attach();
        } catch (Exception e) {
            Log.e(TAG, "Error setting up TabLayoutMediator", e);
        }
    }

    private void setupClickListeners() {
        settings.setOnClickListener(v -> navigateToSettings());
        editProfileBtn.setOnClickListener(v -> navigateToEditProfile());
        fabAddRecipe.setOnClickListener(v -> navigateToAddRecipe());
        
        // Add click listener for follower/following sections if needed
        if (followersCountText != null) {
            followersCountText.setOnClickListener(v -> navigateToFollowersList());
        }
        
        if (followingCountText != null) {
            followingCountText.setOnClickListener(v -> navigateToFollowingList());
        }
    }
    
    private void navigateToFollowersList() {
        // To be implemented - show followers list
        Toast.makeText(getContext(), "Coming soon: Followers list", Toast.LENGTH_SHORT).show();
    }
    
    private void navigateToFollowingList() {
        // To be implemented - show following list
        Toast.makeText(getContext(), "Coming soon: Following list", Toast.LENGTH_SHORT).show();
    }

    private void navigateToSettings() {
        if (isAdded() && getParentFragmentManager() != null) {
            SettingsFragment fragment = new SettingsFragment();
            getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private void navigateToEditProfile() {
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        }
    }

    private void navigateToAddRecipe() {
        if (isAdded() && getParentFragmentManager() != null) {
            AddRecipeFragment fragment = new AddRecipeFragment();
            getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out,
                    android.R.anim.fade_in,
                    android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit();
        }
    }

    private void navigateToLogin() {
        if (isAdded() && getActivity() != null) {
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            refreshUserData();
            fetchUserStats();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Unregister callbacks first
        if (viewPager != null && pageChangeCallback != null) {
            viewPager.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        
        // Detach TabLayoutMediator
        if (tabLayoutMediator != null) {
            tabLayoutMediator.detach();
            tabLayoutMediator = null;
        }
        
        // Clear references
        viewPager = null;
        tabLayout = null;
        tabsAdapter = null;
        isViewCreated = false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_TAB, selectedTab);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState != null) {
            selectedTab = savedInstanceState.getInt(KEY_SELECTED_TAB, DEFAULT_TAB);
        }
    }

    private void refreshUserData() {
        if (user == null || !isAdded()) return;

        showLoading(true);
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!isAdded()) return;

                if (documentSnapshot.exists()) {
                    updateUserInterface(documentSnapshot.getString("name"),
                                     documentSnapshot.getString("bio"),
                                     documentSnapshot.getString("profilePicture"));
                } else {
                    updateUserInterface(user.getDisplayName(), null, null);
                }
                showLoading(false);
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading user profile", e);
                updateUserInterface(user.getDisplayName(), null, null);
                showLoading(false);
            });
    }
    
    private void fetchUserStats() {
        if (user == null || !isAdded()) return;
        
        // 1. Fetch Recipe Count
        FirebaseFirestore.getInstance()
            .collection("recipes")
            .whereEqualTo("userId", user.getUid())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!isAdded()) return;
                
                int recipeCount = querySnapshot.size();
                updateRecipeCount(recipeCount);
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading recipe count", e);
            });
            
        // 2. Fetch Followers Count
        FirebaseFirestore.getInstance()
            .collection("followers")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!isAdded()) return;
                
                if (documentSnapshot.exists() && documentSnapshot.contains("followersList")) {
                    List<String> followers = (List<String>) documentSnapshot.get("followersList");
                    updateFollowersCount(followers != null ? followers.size() : 0);
                } else {
                    updateFollowersCount(0);
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading followers", e);
                updateFollowersCount(0);
            });
            
        // 3. Fetch Following Count
        FirebaseFirestore.getInstance()
            .collection("following")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!isAdded()) return;
                
                if (documentSnapshot.exists() && documentSnapshot.contains("followingList")) {
                    List<String> following = (List<String>) documentSnapshot.get("followingList");
                    updateFollowingCount(following != null ? following.size() : 0);
                } else {
                    updateFollowingCount(0);
                }
            })
            .addOnFailureListener(e -> {
                if (!isAdded()) return;
                Log.e(TAG, "Error loading following", e);
                updateFollowingCount(0);
            });
    }
    
    private void updateRecipeCount(int count) {
        if (recipesCountText != null) {
            recipesCountText.setText(String.valueOf(count));
        }
    }
    
    private void updateFollowersCount(int count) {
        if (followersCountText != null) {
            followersCountText.setText(String.valueOf(count));
        }
    }
    
    private void updateFollowingCount(int count) {
        if (followingCountText != null) {
            followingCountText.setText(String.valueOf(count));
        }
    }

    private void updateUserInterface(String name, String bio, String profilePictureBase64) {
        if (!isAdded()) return;

        try {
            if (hiTxt != null) {
                hiTxt.setText(name != null ? getString(R.string.hello_user_format, name) : getString(R.string.hello_guest));
            }

            if (bioTxt != null) {
                if (bio != null && !bio.isEmpty()) {
                    bioTxt.setText(bio);
                    bioTxt.setVisibility(View.VISIBLE);
                } else {
                    bioTxt.setVisibility(View.GONE);
                }
            }

            if (profileImageView != null) {
                if (profilePictureBase64 != null && !profilePictureBase64.isEmpty()) {
                    try {
                        byte[] decodedString = Base64.decode(profilePictureBase64, Base64.DEFAULT);
                        Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        profileImageView.setImageBitmap(decodedBitmap);
                    } catch (Exception e) {
                        profileImageView.setImageResource(R.drawable.avatar_placeholder);
                        Log.e(TAG, "Error decoding profile image", e);
                    }
                } else if (user.getPhotoUrl() != null) {
                    // Use Glide or another image loading library here
                    profileImageView.setImageResource(R.drawable.avatar_placeholder);
                } else {
                    profileImageView.setImageResource(R.drawable.avatar_placeholder);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating UI", e);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
