package com.example.hotel_v2.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotel_v2.databinding.ItemBookingBinding;
import com.example.hotel_v2.models.Booking;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
    private final List<Booking> bookings;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public BookingAdapter(List<Booking> bookings) {
        this.bookings = bookings;
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBookingBinding binding = ItemBookingBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new BookingViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final ItemBookingBinding binding;

        BookingViewHolder(ItemBookingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Booking booking) {
            binding.hotelName.setText(booking.getHotelName());
            binding.dates.setText(String.format("%s - %s", 
                booking.getCheckInDate(), booking.getCheckOutDate()));
            binding.price.setText(String.format(Locale.getDefault(), "$%.2f", booking.getPrice()));
            binding.status.setText(booking.getStatus());
            binding.bookingDate.setText(dateFormat.format(new Date(booking.getTimestamp())));

            // Set status color based on booking status
            int statusColor;
            switch (booking.getStatus().toLowerCase()) {
                case "confirmed":
                    statusColor = binding.getRoot().getContext().getColor(android.R.color.holo_green_dark);
                    break;
                case "pending":
                    statusColor = binding.getRoot().getContext().getColor(android.R.color.holo_orange_dark);
                    break;
                case "cancelled":
                    statusColor = binding.getRoot().getContext().getColor(android.R.color.holo_red_dark);
                    break;
                default:
                    statusColor = binding.getRoot().getContext().getColor(android.R.color.darker_gray);
            }
            binding.status.setTextColor(statusColor);
        }
    }
} 