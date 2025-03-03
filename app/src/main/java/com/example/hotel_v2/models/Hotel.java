package com.example.hotel_v2.models;

import java.util.List;

public class Hotel {
    private String id;
    private String name;
    private String location;
    private String imageUrl;
    private double price;
    private float rating;
    private int availableRooms;
    private String description;
    private List<String> amenities;

    public Hotel() {
        // Required empty constructor for Firestore
    }

    public Hotel(String id, String name, String location, String imageUrl, double price, float rating, int availableRooms, String description, List<String> amenities) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.price = price;
        this.rating = rating;
        this.availableRooms = availableRooms;
        this.description = description;
        this.amenities = amenities;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public int getAvailableRooms() { return availableRooms; }
    public void setAvailableRooms(int availableRooms) { this.availableRooms = availableRooms; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getAmenities() { return amenities; }
    public void setAmenities(List<String> amenities) { this.amenities = amenities; }
} 