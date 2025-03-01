package com.example.reuvenbagrut;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfileTabsAdapter extends FragmentStateAdapter {

    public ProfileTabsAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Uploaded and Liked
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return new UploadedRecipesFragment();
        } else {
            return new LikedRecipesFragment();
        }
    }
}
