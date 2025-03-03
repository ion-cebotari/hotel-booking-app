# Hotel Booking App

A modern Android application for hotel bookings with Firebase integration.

## Features

- User Authentication (Login/Register)
- Browse available hotels
- View hotel details (images, amenities, prices)
- Book hotel rooms
- View booking history
- Real-time updates using Firebase

## Technologies Used

- Android (Java)
- Firebase Authentication
- Firebase Firestore
- Glide for image loading
- Material Design components

## Setup Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Create a Firebase project and add your `google-services.json`
4. Enable Email/Password authentication in Firebase Console
5. Set up Firestore Database
6. Build and run the application

## Firebase Configuration

Make sure to:
1. Add your own `google-services.json` file in the app directory
2. Enable Authentication in Firebase Console
3. Set up Firestore Database with appropriate security rules

## Database Structure

### Hotels Collection
```json
{
    "name": "Hotel Name",
    "location": "Hotel Location",
    "imageUrl": "Image URL",
    "price": 299.99,
    "rating": 4.5,
    "availableRooms": 15,
    "description": "Hotel Description",
    "amenities": [
        "Free Wi-Fi",
        "Swimming Pool",
        "etc..."
    ]
}
```

### Bookings Collection
```json
{
    "userId": "user_id",
    "hotelId": "hotel_id",
    "checkInDate": "date",
    "checkOutDate": "date",
    "status": "pending/confirmed",
    "timestamp": 1234567890
}
```

## Screenshots
[Add your app screenshots here]

## License
[Add your license information here] 