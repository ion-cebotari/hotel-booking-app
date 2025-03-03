package com.example.hotel_v2;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.hotel_v2.auth.LoginActivity;
import com.example.hotel_v2.databinding.ActivityMainBinding;
import com.example.hotel_v2.fragments.BookingsFragment;
import com.example.hotel_v2.fragments.HomeFragment;
import com.example.hotel_v2.fragments.ProfileFragment;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {
    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        
        // Check if user is logged in
        if (firebaseAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        binding.bottomNavigation.setOnItemSelectedListener(this);
        
        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new HomeFragment())
                .commit();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;
        
        if (item.getItemId() == R.id.menu_home) {
            selectedFragment = new HomeFragment();
        } else if (item.getItemId() == R.id.menu_bookings) {
            selectedFragment = new BookingsFragment();
        } else if (item.getItemId() == R.id.menu_profile) {
            selectedFragment = new ProfileFragment();
        }

        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, selectedFragment)
                    .commit();
            return true;
        }
        
        return false;
    }
}