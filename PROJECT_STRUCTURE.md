# Chatify - Chat Application

## Project Structure Overview

This project follows **Clean Architecture** principles with proper separation of concerns.

### Architecture Layers

```
📁 com.niteshray.xapps.chatify/
├── 📁 feature/
│   ├── 📁 auth/                    # Authentication Feature
│   │   ├── 📁 data/
│   │   │   └── 📁 repository/      # Repository Implementation
│   │   │       └── AuthRepositoryImpl.kt
│   │   ├── 📁 domain/
│   │   │   ├── 📁 model/           # Data Models
│   │   │   │   └── User.kt
│   │   │   └── 📁 repository/      # Repository Interface
│   │   │       └── AuthRepository.kt
│   │   └── 📁 presentation/        # UI Layer
│   │       ├── AuthViewModel.kt
│   │       ├── LoginFragment.kt
│   │       └── SignupFragment.kt
│   │
│   └── 📁 home/                    # Home Feature
│       ├── 📁 data/
│       │   └── 📁 repository/
│       │       └── HomeRepositoryImpl.kt
│       ├── 📁 domain/
│       │   ├── 📁 model/
│       │   │   └── Friend.kt
│       │   └── 📁 repository/
│       │       └── HomeRepository.kt
│       └── 📁 presentation/
│           ├── 📁 adapter/
│           │   ├── FriendsAdapter.kt
│           │   └── UserSearchAdapter.kt
│           ├── HomeViewModel.kt
│           └── HomeFragment.kt
│
└── MainActivity.kt
```

## Features Implemented

### ✅ Authentication Module
1. **Login Screen**
   - Email & password validation
   - Firebase Authentication integration
   - Navigate to home on successful login
   - Proper error handling

2. **Signup Screen**
   - User registration with name, email, password
   - Store user data in Realtime Database
   - Auto-navigate to home after signup
   - Input validation

3. **Repository Pattern**
   - `AuthRepository` interface for abstraction
   - `AuthRepositoryImpl` for Firebase Realtime Database operations
   - Clean separation of concerns

### ✅ Home Screen
1. **Friends List**
   - RecyclerView with custom adapter
   - Display friend's name, email, online status
   - Real-time updates using Realtime Database listeners
   - Empty state handling

2. **Add Friend Feature**
   - Search users by name
   - Material Dialog with search functionality
   - Add friend button in each search result
   - Two-way friend relationship (bidirectional)

3. **Top App Bar**
   - App title "Chatify"
   - Add friend icon button
   - Logout option in overflow menu

### ✅ Navigation
- Navigation Component with NavGraph
- Proper navigation flow:
  - Login → Signup
  - Login → Home (after auth)
  - Signup → Home (after auth)
  - Home → Login (after logout)
- Pop backstack properly to prevent returning to auth screens

## Data Models

### User Model
```kotlin
data class User(
    val uid: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String,
    val isOnline: Boolean,
    val lastSeen: Long,
    val friends: List<String>  // List of friend UIDs
)
```

### Friend Model
```kotlin
data class Friend(
    val uid: String,
    val name: String,
    val email: String,
    val profilePictureUrl: String,
    val isOnline: Boolean,
    val lastSeen: Long,
    val lastMessage: String,
    val lastMessageTime: Long
)
```

## Realtime Database Structure

```
users/
  └── {userId}/
      ├── uid: String
      ├── name: String
      ├── email: String
      ├── profilePictureUrl: String
      ├── isOnline: Boolean
      ├── lastSeen: Long
      └── friends/
          ├── 0: String (friend UID)
          ├── 1: String (friend UID)
          └── ...
```

## Key Technologies

- **Kotlin** - Primary language
- **MVVM Architecture** - ViewModels + LiveData
- **Navigation Component** - For fragment navigation
- **Firebase Authentication** - User authentication
- **Firebase Realtime Database** - Real-time cloud database
- **View Binding** - Type-safe view access
- **Coroutines** - Asynchronous operations
- **Material Design 3** - UI components

## Next Steps (TODO)

### Chat Screen Implementation
1. Create ChatFragment
2. Create ChatViewModel
3. Create message RecyclerView adapter
4. Implement real-time messaging with Firestore
5. Add message input field
6. Add send button
7. Store messages in Realtime Database:
   ```
   chats/
     └── {chatId}/
         └── messages/
             └── {messageId}/
                 ├── senderId: String
                 ├── message: String
                 ├── timestamp: Long
                 └── isRead: Boolean
   ```

8. Navigate from HomeFragment to ChatFragment on friend click
9. Display chat history
10. Real-time message updates

### Additional Features (Future)
- Profile picture upload
- Push notifications
- Typing indicators
- Message read receipts
- Image sharing
- Voice messages
- Group chats
- Search messages
- Delete messages
- Block users

## How to Run

1. Ensure you have Firebase project set up
2. Add `google-services.json` to `app/` directory
3. Sync Gradle
4. Run the app on an emulator or device

## Best Practices Followed

✅ Clean Architecture with separation of concerns  
✅ Repository pattern for data access  
✅ MVVM pattern for UI  
✅ ViewBinding for type-safe views  
✅ Coroutines for async operations  
✅ Proper error handling  
✅ LiveData for reactive UI updates  
✅ Material Design guidelines  
✅ Navigation Component best practices  
✅ Proper resource organization  

## Notes

- The app currently doesn't implement chat messaging - only friend management
- User profile pictures use placeholder icons
- No image upload functionality yet
- Chat screen navigation is prepared but shows a toast message
