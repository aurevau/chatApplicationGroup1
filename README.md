# Chat Application

A real-time messaging Android application built with Kotlin and Firebase.

## Features

- 🔐 User authentication (Email/Password login)
- 🔓 Google Sign-In authentication
- 💬 One-to-one chat messaging
- 👥 Group chat functionality
- 📸 Send images in chat (camera or gallery)
- 🔍 Search users
- 👫 Friend system (send/accept/decline friend requests)
- 🗑️ Delete messages and chats
- ⏱️ Real-time message updates
- 📱 Profile management with profile pictures

## Technologies Used

- **Language:** Kotlin
- **Architecture:** MVVM (Model-View-ViewModel)
- **Backend:** Firebase
    - Firebase Authentication
    - Cloud Firestore (database)
    - Firebase Storage (image storage)
- **UI:** ViewBinding, RecyclerView, Fragments
- **Image Loading:** Glide

## Project Structure

```app/
├── adapter/ RecyclerView adapters
├── data/  Data models (User, Message, ChatRoom)
├── repository/  Firebase data operations
├── ui/  Activities and Fragments
├── viewmodel/  ViewModels for MVVM
├── util/   Utility classes (DateUtils)
└── popup/   Dialog fragments
```

## Setup Instructions

### Prerequisites
- Android Studio installed
- Firebase project created
- `google-services.json` file from Firebase

### Installation Steps

1. Clone this repository
   ```bash
   git clone [your-repo-url]
### How To Use

- Register/Login: Create account or login with existing credentials
- Search Users: Find other users to chat with
- Add Friends: Send and accept friend requests
- Start Chat: Click on a user to start chatting
- Send Messages: Type text or send images
- Delete: Long-press messages or chats to delete
- Create Group: Select multiple users and start group chat

### Firestore Database Structure

```
users/
  ├── {userId}/
  │     ├── fullName: String
  │     ├── email: String
  │     ├── profileImageUrl: String
  │     ├── friends/
  │     └── recentSearches/

chatRooms/
  ├── {roomId}/
  │     ├── members: [userId1, userId2]
  │     ├── lastMessage: String
  │     ├── lastMessageTimestamp: Long
  │     ├── isGroup: Boolean
  │     └── messages/
  │           └── {messageId}/
  │                 ├── text: String
  │                 ├── imageUrl: String
  │                 ├── senderId: String
  │                 └── timestamp: Long
```


