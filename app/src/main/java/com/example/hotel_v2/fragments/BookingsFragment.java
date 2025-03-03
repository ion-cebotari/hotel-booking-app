package com.example.hotel_v2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BookingsFragment extends Fragment {
    private FragmentBookingsBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;
    private BookingAdapter bookingAdapter;
    private List<Booking> bookings;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentBookingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        bookings = new ArrayList<>();
        
        setupRecyclerView();
        loadBookings();
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(bookings);
        binding.bookingsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.bookingsRecyclerView.setAdapter(bookingAdapter);
    }

    private void loadBookings() {
        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(requireContext(), "Please login to view bookings", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        String userId = firebaseAuth.getCurrentUser().getUid();

        firestore.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    binding.progressBar.setVisibility(View.GONE);
                    
                    if (error != null) {
                        Toast.makeText(requireContext(), "Error: " + error.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        bookings.clear();
                        for (QueryDocumentSnapshot document : value) {
                            Booking booking = document.toObject(Booking.class);
                            booking.setId(document.getId());
                            bookings.add(booking);
                        }
                        
                        if (bookings.isEmpty()) {
                            binding.noBookingsText.setVisibility(View.VISIBLE);
                        } else {
                            binding.noBookingsText.setVisibility(View.GONE);
                        }
                        
                        bookingAdapter.notifyDataSetChanged();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 