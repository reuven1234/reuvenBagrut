package com.example.reuvenbagrut;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.reuvenbagrut.databinding.FragmentProfileNavBinding;

public class Profile_nav_Fragment extends Fragment {

    public Profile_nav_Fragment() {
        // Required empty public constructor
    }

    ImageButton settings;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings_nav_, container, false);

        settings.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Profile_nav_Fragment fragment1 = new Profile_nav_Fragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment,fragment1);
                transaction.commit();
            }
        });

        return view;
    }
}