package com.example.hotel_v2.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_v2.R;
import com.example.hotel_v2.activities.HotelDetailActivity;
import com.example.hotel_v2.models.Booking;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private final Context context;
    private final List<Booking> bookings;
    private final FirebaseFirestore db;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public BookingAdapter(Context context, List<Booking> bookings) {
        this.context = context;
        this.bookings = bookings;
        this.db = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);

        // Load hotel details
        db.collection("hotels").document(booking.getHotelId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String hotelName = documentSnapshot.getString("name");
                    holder.hotelName.setText(hotelName);
                });

        // Set booking details
        holder.checkInDate.setText("Check-in: " + booking.getCheckInDate());
        holder.checkOutDate.setText("Check-out: " + booking.getCheckOutDate());
        holder.bookingStatus.setText(booking.getStatus().toUpperCase());

        // Set status color
        int statusColor;
        switch (booking.getStatus().toLowerCase()) {
            case "confirmed":
                statusColor = context.getColor(android.R.color.holo_green_dark);
                break;
            case "cancelled":
                statusColor = context.getColor(android.R.color.holo_red_dark);
                break;
            default:
                statusColor = context.getColor(android.R.color.holo_orange_dark);
                break;
        }
        holder.bookingStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(statusColor));

        // Handle view button click
        holder.viewButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, HotelDetailActivity.class);
            intent.putExtra("hotel_id", booking.getHotelId());
            context.startActivity(intent);
        });

        // Handle cancel button
        holder.cancelButton.setVisibility(booking.getStatus().equals("pending") ? View.VISIBLE : View.GONE);
        holder.cancelButton.setOnClickListener(v -> cancelBooking(booking, position));
    }

    private void cancelBooking(Booking booking, int position) {
        db.collection("bookings").document(booking.getId())
                .update("status", "cancelled")
                .addOnSuccessListener(aVoid -> {
                    booking.setStatus("cancelled");
                    notifyItemChanged(position);
                    Toast.makeText(context, "Booking cancelled successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(context, "Failed to cancel booking: " + e.getMessage(), 
                            Toast.LENGTH_SHORT).show());
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    public void updateBookings(List<Booking> newBookings) {
        bookings.clear();
        bookings.addAll(newBookings);
        notifyDataSetChanged();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView hotelName;
        TextView checkInDate;
        TextView checkOutDate;
        TextView bookingStatus;
        MaterialButton viewButton;
        MaterialButton cancelButton;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            hotelName = itemView.findViewById(R.id.hotelName);
            checkInDate = itemView.findViewById(R.id.checkInDate);
            checkOutDate = itemView.findViewById(R.id.checkOutDate);
            bookingStatus = itemView.findViewById(R.id.bookingStatus);
            viewButton = itemView.findViewById(R.id.viewButton);
            cancelButton = itemView.findViewById(R.id.cancelButton);
        }
    }
} 