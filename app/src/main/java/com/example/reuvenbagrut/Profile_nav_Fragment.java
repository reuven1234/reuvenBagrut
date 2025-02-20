package com.example.reuvenbagrut;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.SurfaceControl;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.reuvenbagrut.databinding.FragmentProfileNavBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Profile_nav_Fragment extends Fragment {
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    TextView hiTxt;

    public Profile_nav_Fragment() {
        // Required empty public constructor
    }

    ImageButton settings;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_nav_, container, false);
        settings = view.findViewById(R.id.settings);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Settings_nav_Fragment fragment1 = new Settings_nav_Fragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment,fragment1);
                transaction.commit();
            }
        });

        if(user != null)
        {
            String name = "";
            name = user.getDisplayName();
            hiTxt.setText("Hello " + name);
        }

        return view;
    }
}