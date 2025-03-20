package com.example.reuvenbagrut;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import android.content.Context;
import android.os.Bundle;

public class ProfileTabsAdapter extends FragmentStateAdapter {
    private static final int UPLOADED_POSITION = 0;
    private static final int LIKED_POSITION = 1;
    private static final int NUM_TABS = 2;

    private final Context context;

    public ProfileTabsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        this.context = fragmentActivity;
    }

    public ProfileTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
        this.context = fragment.requireContext();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Bundle args = new Bundle();
        Fragment fragment;
        
        switch (position) {
            case UPLOADED_POSITION:
                fragment = new UploadedRecipesFragment();
                args.putString("type", "uploaded");
                break;
            case LIKED_POSITION:
                fragment = new LikedRecipesFragment();
                args.putString("type", "liked");
                break;
            default:
                throw new IllegalStateException("Invalid tab position: " + position);
        }
        
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }

    @StringRes
    private int getTabTitleResId(int position) {
        switch (position) {
            case UPLOADED_POSITION:
                return R.string.tab_uploaded;
            case LIKED_POSITION:
                return R.string.tab_liked;
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    public String getTabTitle(int position) {
        try {
            return context.getString(getTabTitleResId(position));
        } catch (Exception e) {
            return position == UPLOADED_POSITION ? "Uploaded" : "Liked";
        }
    }

    @Override
    public long getItemId(int position) {
        // Use stable IDs based on fragment type
        return position == UPLOADED_POSITION ? 
            UploadedRecipesFragment.class.hashCode() : 
            LikedRecipesFragment.class.hashCode();
    }

    @Override
    public boolean containsItem(long itemId) {
        return itemId == UploadedRecipesFragment.class.hashCode() || 
               itemId == LikedRecipesFragment.class.hashCode();
    }
}
