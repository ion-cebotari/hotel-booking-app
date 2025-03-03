package com.example.hotel_v2.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_v2.R;
import com.example.hotel_v2.adapters.HotelAdapter;
import com.example.hotel_v2.models.Hotel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private HotelAdapter adapter;
    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        recyclerView = view.findViewById(R.id.hotelsRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HotelAdapter(getContext(), new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Load hotels
        loadHotels();
    }

    private void loadHotels() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("hotels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Hotel> hotels = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Hotel hotel = document.toObject(Hotel.class);
                        hotel.setId(document.getId());
                        hotels.add(hotel);
                    }
                    adapter.updateHotels(hotels);
                    progressBar.setVisibility(View.GONE);

                    if (hotels.isEmpty()) {
                        Toast.makeText(getContext(), "No hotels found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Error loading hotels: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show();
                });
    }
} 