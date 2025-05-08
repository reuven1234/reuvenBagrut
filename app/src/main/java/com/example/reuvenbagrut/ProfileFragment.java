package com.example.reuvenbagrut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.appbar.MaterialToolbar;

public class ProfileFragment extends Fragment {

    private TabLayout tabs;
    private ViewPager2 viewPager;
    private ProfileTabsAdapter adapter;
    private FloatingActionButton fabAddRecipe;
    private MaterialToolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the same layout you posted (save it as fragment_profile.xml)
        View view = inflater.inflate(
                R.layout.fragment_profile, container, false
        );

        toolbar       = view.findViewById(R.id.toolbar);
        fabAddRecipe  = view.findViewById(R.id.fabAddRecipe);
        tabs          = view.findViewById(R.id.tabs);
        viewPager     = view.findViewById(R.id.viewPager);

        // Set up adapter & connect tabs
        adapter = new ProfileTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabs, viewPager,
                (tab, position) -> tab.setText(adapter.getTabTitle(position))
        ).attach();

        // TODO: hook up toolbar actions and fabAddRecipe click here
        return view;
    }
}
