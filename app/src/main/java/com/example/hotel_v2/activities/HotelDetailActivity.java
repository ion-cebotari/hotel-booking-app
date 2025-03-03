package com.example.hotel_v2.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.hotel_v2.R;
import com.example.hotel_v2.databinding.ActivityHotelDetailBinding;
import com.example.hotel_v2.models.Hotel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HotelDetailActivity extends AppCompatActivity {
    private ActivityHotelDetailBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private String hotelId;
    private Hotel hotel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        hotelId = getIntent().getStringExtra("hotel_id");

        if (hotelId == null) {
            Toast.makeText(this, "Error loading hotel details", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.bookButton.setOnClickListener(v -> bookHotel());
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadHotelDetails();
    }

    private void loadHotelDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);

        firestore.collection("hotels").document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    hotel = documentSnapshot.toObject(Hotel.class);
                    if (hotel != null) {
                        updateUI();
                    } else {
                        Toast.makeText(this, "Hotel not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void updateUI() {
        binding.hotelName.setText(hotel.getName());
        binding.hotelLocation.setText(hotel.getLocation());
        binding.hotelDescription.setText(hotel.getDescription());
        binding.hotelPrice.setText(String.format(Locale.getDefault(), "$%.2f per night", hotel.getPrice()));
        binding.ratingBar.setRating((float) hotel.getRating());
        binding.availableRooms.setText(String.format(Locale.getDefault(), 
            "%d rooms available", hotel.getAvailableRooms()));

        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(hotel.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(binding.hotelImage);
        }

        // Set up amenities
        StringBuilder amenitiesText = new StringBuilder();
        if (hotel.getAmenities() != null) {
            for (String amenity : hotel.getAmenities()) {
                amenitiesText.append("• ").append(amenity).append("\n");
            }
            binding.amenitiesList.setText(amenitiesText.toString().trim());
        }

        binding.bookButton.setEnabled(hotel.getAvailableRooms() > 0);
    }

    private void bookHotel() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login to book a hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.bookButton.setEnabled(false);

        String userId = firebaseAuth.getCurrentUser().getUid();
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", userId);
        booking.put("hotelId", hotelId);
        booking.put("hotelName", hotel.getName());
        booking.put("checkInDate", binding.checkInDate.getText().toString());
        booking.put("checkOutDate", binding.checkOutDate.getText().toString());
        booking.put("price", hotel.getPrice());
        booking.put("status", "pending");
        booking.put("timestamp", System.currentTimeMillis());

        firestore.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    // Update available rooms
                    firestore.collection("hotels").document(hotelId)
                            .update("availableRooms", hotel.getAvailableRooms() - 1)
                            .addOnSuccessListener(aVoid -> {
                                binding.progressBar.setVisibility(View.GONE);
                                Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                binding.progressBar.setVisibility(View.GONE);
                                binding.bookButton.setEnabled(true);
                                Toast.makeText(this, "Error updating room availability", 
                                    Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.bookButton.setEnabled(true);
                    Toast.makeText(this, "Error creating booking", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 