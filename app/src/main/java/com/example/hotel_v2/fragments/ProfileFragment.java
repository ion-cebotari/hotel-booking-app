package com.example.hotel_v2.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.hotel_v2.auth.LoginActivity;
import com.example.hotel_v2.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        loadUserProfile();
        setupListeners();
    }

    private void loadUserProfile() {
        String userId = firebaseAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);

        firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        binding.nameEditText.setText(documentSnapshot.getString("name"));
                        binding.emailEditText.setText(documentSnapshot.getString("email"));
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Error loading profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void setupListeners() {
        binding.updateButton.setOnClickListener(v -> updateProfile());
        binding.logoutButton.setOnClickListener(v -> logout());
    }

    private void updateProfile() {
        String name = binding.nameEditText.getText().toString().trim();
        if (name.isEmpty()) {
            binding.nameEditText.setError("Name is required");
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.updateButton.setEnabled(false);

        firestore.collection("users").document(userId)
                .update("name", name)
                .addOnSuccessListener(aVoid -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.updateButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Profile updated successfully",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.updateButton.setEnabled(true);
                    Toast.makeText(requireContext(), "Error updating profile: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    private void logout() {
        firebaseAuth.signOut();
        startActivity(new Intent(requireContext(), LoginActivity.class));
        requireActivity().finishAffinity();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 