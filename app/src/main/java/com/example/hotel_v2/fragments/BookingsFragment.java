package com.example.hotel_v2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.hotel_v2.adapters.BookingAdapter;
import com.example.hotel_v2.databinding.FragmentBookingsBinding;
import com.example.hotel_v2.models.Booking;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.ConnectionResult;

import java.util.ArrayList;
import java.util.List;

public class BookingsFragment extends Fragment {
    private FragmentBookingsBinding binding;
    private BookingAdapter bookingAdapter;
    private FirebaseFirestore db;
    private List<Booking> bookings;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!checkPlayServices()) {
            Toast.makeText(requireContext(), "Google Play Services not available", Toast.LENGTH_LONG).show();
            return;
        }

        db = FirebaseFirestore.getInstance();
        bookings = new ArrayList<>();

        // Setup RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        bookingAdapter = new BookingAdapter(requireContext(), bookings);
        binding.recyclerView.setAdapter(bookingAdapter);

        // Load bookings
        loadBookings();
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(requireContext());
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(requireActivity(), resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Log.e("BookingsFragment", "This device does not support Google Play Services");
            }
            return false;
        }
        return true;
    }

    private void loadBookings() {
        // Check if user is authenticated
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login to view bookings", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
        binding.emptyView.setVisibility(View.GONE);

        try {
            db.collection("bookings")
                    .whereEqualTo("userId", userId)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        binding.progressBar.setVisibility(View.GONE);
                        bookings.clear();
                        
                        for (var document : queryDocumentSnapshots) {
                            Booking booking = document.toObject(Booking.class);
                            if (booking != null) {
                                booking.setId(document.getId());
                                bookings.add(booking);
                            }
                        }

                        if (bookings.isEmpty()) {
                            binding.emptyView.setVisibility(View.VISIBLE);
                            binding.recyclerView.setVisibility(View.GONE);
                        } else {
                            binding.emptyView.setVisibility(View.GONE);
                            binding.recyclerView.setVisibility(View.VISIBLE);
                            bookingAdapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                        
                        String message = e.getMessage();
                        if (message != null && message.contains("FAILED_PRECONDITION") && message.contains("requires an index")) {
                            // Show a more user-friendly message while index is being built
                            Toast.makeText(requireContext(), 
                                "First-time setup in progress. Please try again in a few minutes.", 
                                Toast.LENGTH_LONG).show();
                            
                            // Try loading without ordering as a fallback
                            loadBookingsWithoutOrder();
                        } else {
                            String errorMessage = "Error loading bookings: " + message;
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                            Log.e("BookingsFragment", errorMessage, e);
                        }
                    });
        } catch (Exception e) {
            Log.e("BookingsFragment", "Error initializing Firestore query", e);
            Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadBookingsWithoutOrder() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        db.collection("bookings")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    binding.progressBar.setVisibility(View.GONE);
                    bookings.clear();
                    
                    for (var document : queryDocumentSnapshots) {
                        Booking booking = document.toObject(Booking.class);
                        if (booking != null) {
                            booking.setId(document.getId());
                            bookings.add(booking);
                        }
                    }

                    if (bookings.isEmpty()) {
                        binding.emptyView.setVisibility(View.VISIBLE);
                        binding.recyclerView.setVisibility(View.GONE);
                    } else {
                        binding.emptyView.setVisibility(View.GONE);
                        binding.recyclerView.setVisibility(View.VISIBLE);
                        bookingAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.emptyView.setVisibility(View.VISIBLE);
                    
                    String errorMessage = "Error loading bookings: " + e.getMessage();
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    Log.e("BookingsFragment", errorMessage, e);
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 