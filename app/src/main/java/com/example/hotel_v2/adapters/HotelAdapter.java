package com.example.hotel_v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hotel_v2.R;
import com.example.hotel_v2.activities.HotelDetailActivity;
import com.example.hotel_v2.models.Hotel;

import java.util.List;
import java.util.Locale;

public class HotelAdapter extends RecyclerView.Adapter<HotelAdapter.HotelViewHolder> {
    private final Context context;
    private final List<Hotel> hotels;

    public HotelAdapter(Context context, List<Hotel> hotels) {
        this.context = context;
        this.hotels = hotels;
    }

    @NonNull
    @Override
    public HotelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_hotel, parent, false);
        return new HotelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HotelViewHolder holder, int position) {
        Hotel hotel = hotels.get(position);

        // Load hotel image using Glide
        if (hotel.getImageUrl() != null && !hotel.getImageUrl().isEmpty()) {
            Glide.with(context)
                    .load(hotel.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.error_image)
                    .into(holder.hotelImage);
        }

        // Set hotel details
        holder.hotelName.setText(hotel.getName());
        holder.hotelLocation.setText(hotel.getLocation());
        holder.hotelPrice.setText(String.format(Locale.getDefault(), "$%.2f", hotel.getPrice()));
        holder.ratingBar.setRating(hotel.getRating());
        holder.availableRooms.setText(String.format(Locale.getDefault(), "%d rooms available", hotel.getAvailableRooms()));

        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            intent.putExtra("hotel_id", hotel.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return hotels.size();
    }

    public void updateHotels(List<Hotel> newHotels) {
        hotels.clear();
        hotels.addAll(newHotels);
        notifyDataSetChanged();
    }

    static class HotelViewHolder extends RecyclerView.ViewHolder {
        ImageView hotelImage;
        TextView hotelName;
        TextView hotelLocation;
        TextView hotelPrice;
        RatingBar ratingBar;
        TextView availableRooms;

        public HotelViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelImage = itemView.findViewById(R.id.hotelImage);
            hotelName = itemView.findViewById(R.id.hotelName);
            hotelLocation = itemView.findViewById(R.id.hotelLocation);
            hotelPrice = itemView.findViewById(R.id.hotelPrice);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            availableRooms = itemView.findViewById(R.id.availableRooms);
        }
    }
} 