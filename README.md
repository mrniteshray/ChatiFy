# ChatiFy

A modern Android chat application built with Kotlin, featuring real-time messaging and user authentication.

## Features

- **User Authentication**: Secure login and registration using Firebase Auth
- **Real-time Messaging**: Chat with friends in real-time
- **Friend Connections**: Connect with other users via unique usernames
- **Modern UI**: Clean and intuitive Material Design interface

## Tech Stack

### Core
- **Language**: Kotlin
- **Min SDK**: 28 (Android 9.0)
- **Target SDK**: 36

### Architecture
- Clean Architecture (Data, Domain, Presentation layers)
- MVVM Pattern
- Navigation Component

### Libraries & Frameworks

**Firebase**
- Firebase Authentication
- Firebase Realtime Database
- Firebase Firestore

**Android Jetpack**
- Navigation Component
- ViewModel & LiveData
- Fragment KTX
- ViewBinding

**Concurrency**
- Kotlin Coroutines

**UI**
- Material Design Components
- ConstraintLayout

## Project Structure

```
chatify/
├── feature/
│   ├── auth/          # Authentication module
│   ├── chat/          # Messaging functionality
│   └── home/          # Home screen
└── MainActivity.kt
```

Each feature follows Clean Architecture:
- `data/` - Data sources and repositories
- `domain/` - Business logic and models
- `presentation/` - UI layer (Fragments, ViewModels)

## Setup

1. Clone the repository
2. Add your `google-services.json` file to the `app/` directory
3. Sync the project with Gradle
4. Run the app

## Build

```bash
./gradlew assembleDebug
```

## Requirements

- Android Studio Koala or newer
- JDK 11
- Gradle 8.12.3+
