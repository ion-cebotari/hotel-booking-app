package com.example.hotel_v2.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.hotel_v2.MainActivity;
import com.example.hotel_v2.databinding.ActivityRegisterBinding;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";
    private ActivityRegisterBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        try {
            // Ensure Firebase is initialized
            FirebaseApp.initializeApp(this);
            
            firebaseAuth = FirebaseAuth.getInstance();
            firestore = FirebaseFirestore.getInstance();
            
            Log.d(TAG, "Firebase Auth initialized: " + (firebaseAuth != null));
            Log.d(TAG, "Firebase Firestore initialized: " + (firestore != null));
        } catch (Exception e) {
            Log.e(TAG, "Error initializing Firebase: " + e.getMessage(), e);
            Toast.makeText(this, "Error initializing Firebase: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        binding.registerButton.setOnClickListener(v -> registerUser());
        binding.loginTextView.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        if (firebaseAuth == null) {
            Log.e(TAG, "FirebaseAuth not initialized");
            Toast.makeText(this, "Firebase not properly initialized", Toast.LENGTH_LONG).show();
            return;
        }

        String name = binding.nameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        // Full Name validation
        if (name.isEmpty()) {
            binding.nameEditText.setError("Name is required");
            binding.nameEditText.requestFocus();
            return;
        }

        if (name.length() < 3) {
            binding.nameEditText.setError("Name must be at least 3 characters long");
            binding.nameEditText.requestFocus();
            return;
        }

        if (!name.matches("^[a-zA-Z\\s]+$")) {
            binding.nameEditText.setError("Name can only contain letters and spaces");
            binding.nameEditText.requestFocus();
            return;
        }

        // Email validation
        if (email.isEmpty()) {
            binding.emailEditText.setError("Email is required");
            binding.emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.setError("Please enter a valid email address");
            binding.emailEditText.requestFocus();
            return;
        }

        if (email.length() > 254) {
            binding.emailEditText.setError("Email address is too long");
            binding.emailEditText.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            binding.passwordEditText.setError("Password is required");
            binding.passwordEditText.requestFocus();
            return;
        }

        // Password validation rules
        if (password.length() < 6) {
            binding.passwordEditText.setError("Password must be at least 6 characters long");
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.matches(".*[A-Z].*")) {
            binding.passwordEditText.setError("Password must contain at least one uppercase letter");
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.matches(".*[a-z].*")) {
            binding.passwordEditText.setError("Password must contain at least one lowercase letter");
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.matches(".*\\d.*")) {
            binding.passwordEditText.setError("Password must contain at least one number");
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?].*")) {
            binding.passwordEditText.setError("Password must contain at least one special character");
            binding.passwordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEditText.setError("Passwords do not match");
            binding.confirmPasswordEditText.requestFocus();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.registerButton.setEnabled(false);

        Log.d(TAG, "Attempting to create user with email: " + email);
        
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "User creation successful");
                        String userId = firebaseAuth.getCurrentUser().getUid();
                        Map<String, Object> user = new HashMap<>();
                        user.put("name", name);
                        user.put("email", email);
                        user.put("userId", userId);

                        firestore.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "User profile created successfully");
                                    binding.progressBar.setVisibility(View.GONE);
                                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                                    finishAffinity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error creating user profile", e);
                                    binding.progressBar.setVisibility(View.GONE);
                                    binding.registerButton.setEnabled(true);
                                    Toast.makeText(RegisterActivity.this,
                                            "Error creating user profile: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.registerButton.setEnabled(true);
                        
                        try {
                            Exception exception = task.getException();
                            Log.e(TAG, "Registration failed", exception);
                            throw exception;
                        } catch (FirebaseAuthWeakPasswordException e) {
                            binding.passwordEditText.setError("Password is too weak");
                            binding.passwordEditText.requestFocus();
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            binding.emailEditText.setError("Invalid email format");
                            binding.emailEditText.requestFocus();
                        } catch (FirebaseAuthUserCollisionException e) {
                            binding.emailEditText.setError("Email already in use");
                            binding.emailEditText.requestFocus();
                        } catch (Exception e) {
                            Log.e(TAG, "Unknown registration error", e);
                            Toast.makeText(RegisterActivity.this,
                                    "Registration failed: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
} 