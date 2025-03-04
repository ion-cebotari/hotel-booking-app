package com.example.hotel_v2.activities;

import android.app.DatePickerDialog;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HotelDetailActivity extends AppCompatActivity {
    private ActivityHotelDetailBinding binding;
    private FirebaseFirestore db;
    private String hotelId;
    private Calendar checkInDate = Calendar.getInstance();
    private Calendar checkOutDate = Calendar.getInstance();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHotelDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get hotel ID from intent
        hotelId = getIntent().getStringExtra("hotel_id");
        if (hotelId == null) {
            Toast.makeText(this, "Error: Hotel not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set up date pickers
        setupDatePickers();

        // Load hotel details
        loadHotelDetails();

        // Set up book button
        binding.bookButton.setOnClickListener(v -> handleBooking());
    }

    private void setupDatePickers() {
        // Initialize check-out date to tomorrow
        checkOutDate.add(Calendar.DAY_OF_MONTH, 1);

        // Set initial dates
        binding.checkInDate.setText(dateFormat.format(checkInDate.getTime()));
        binding.checkOutDate.setText(dateFormat.format(checkOutDate.getTime()));

        // Setup check-in date picker
        binding.checkInDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkInDate.set(year, month, dayOfMonth);
                    binding.checkInDate.setText(dateFormat.format(checkInDate.getTime()));
                    
                    // If check-out date is before check-in date, update it
                    if (checkOutDate.before(checkInDate)) {
                        checkOutDate.setTime(checkInDate.getTime());
                        checkOutDate.add(Calendar.DAY_OF_MONTH, 1);
                        binding.checkOutDate.setText(dateFormat.format(checkOutDate.getTime()));
                    }
                },
                checkInDate.get(Calendar.YEAR),
                checkInDate.get(Calendar.MONTH),
                checkInDate.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date to today
            dialog.getDatePicker().setMinDate(System.currentTimeMillis());
            dialog.show();
        });

        // Setup check-out date picker
        binding.checkOutDate.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    checkOutDate.set(year, month, dayOfMonth);
                    binding.checkOutDate.setText(dateFormat.format(checkOutDate.getTime()));
                },
                checkOutDate.get(Calendar.YEAR),
                checkOutDate.get(Calendar.MONTH),
                checkOutDate.get(Calendar.DAY_OF_MONTH)
            );
            
            // Set minimum date to check-in date + 1 day
            Calendar minDate = Calendar.getInstance();
            minDate.setTime(checkInDate.getTime());
            minDate.add(Calendar.DAY_OF_MONTH, 1);
            dialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
            dialog.show();
        });
    }

    private void handleBooking() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to book a hotel", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate dates
        if (checkInDate.after(checkOutDate)) {
            Toast.makeText(this, "Check-in date must be before check-out date", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create booking
        Map<String, Object> booking = new HashMap<>();
        booking.put("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
        booking.put("hotelId", hotelId);
        booking.put("checkInDate", dateFormat.format(checkInDate.getTime()));
        booking.put("checkOutDate", dateFormat.format(checkOutDate.getTime()));
        booking.put("status", "pending");
        booking.put("timestamp", System.currentTimeMillis());

        // Save booking to Firestore
        db.collection("bookings")
                .add(booking)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Booking successful!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Booking failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadHotelDetails() {
        binding.progressBar.setVisibility(View.VISIBLE);

        db.collection("hotels").document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Hotel hotel = documentSnapshot.toObject(Hotel.class);
                    
                    if (hotel != null) {
                        // Load hotel image
                        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
                            Glide.with(this)
                                    .load(hotel.getImageUrl())
                                    .placeholder(R.drawable.placeholder_image)
                                    .error(R.drawable.error_image)
                                    .into(binding.hotelImage);
                        }

                        // Set hotel details
                        binding.hotelName.setText(hotel.getName());
                        binding.hotelLocation.setText(hotel.getLocation());
                        binding.hotelPrice.setText(String.format("$%.2f per night", hotel.getPrice()));
                        binding.ratingBar.setRating(hotel.getRating());
                        binding.availableRooms.setText(String.format("%d rooms available", hotel.getAvailableRooms()));
                        binding.hotelDescription.setText(hotel.getDescription());

                        // Enable booking button if rooms are available
                        binding.bookButton.setEnabled(hotel.getAvailableRooms() > 0);
                    } else {
                        Toast.makeText(this, "Error: Hotel not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error loading hotel details: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
} 