package com.example.reuvenbagrut.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.reuvenbagrut.UploadedRecipesFragment;
import com.example.reuvenbagrut.LikedRecipesFragment;

public class ProfileTabsAdapter extends FragmentStateAdapter {

    public ProfileTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new UploadedRecipesFragment();
            case 1:
                return new LikedRecipesFragment();
            default:
                throw new IllegalArgumentException("Invalid position: " + position);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
} 