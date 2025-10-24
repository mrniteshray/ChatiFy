<div align="center">

# 💬 ChatiFy

### A Modern Real-Time Chat Application

*Built with Kotlin • Powered by Firebase*

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

</div>

---

## ✨ Features

<table>
<tr>
<td>

- 🔐 **Secure Authentication** - Firebase Auth integration
- 💬 **Real-time Messaging** - Instant message delivery
- 👥 **Friend Connections** - Connect via unique usernames
- 🎨 **Material Design** - Modern & intuitive UI

</td>
</tr>
</table>

## 🛠️ Tech Stack

<details open>
<summary><b>Core Technologies</b></summary>

- ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white) **Kotlin**
- 🎯 **Min SDK**: 28 (Android 9.0)
- 🚀 **Target SDK**: 36

</details>

<details open>
<summary><b>Architecture</b></summary>

- 🏗️ **Clean Architecture** (Data, Domain, Presentation)
- 📐 **MVVM Pattern**
- 🧭 **Navigation Component**

</details>

<details open>
<summary><b>Libraries & Frameworks</b></summary>

**Firebase**
- 🔑 Authentication
- 📊 Realtime Database
- 🗄️ Firestore

**Android Jetpack**
- 🧭 Navigation Component
- 🔄 ViewModel & LiveData
- 🧩 Fragment KTX
- 🔗 ViewBinding

**Concurrency**
- ⚡ Kotlin Coroutines
- 🎮 Coroutines Play Services

**UI Components**
- 🎨 Material Design 3
- 📱 ConstraintLayout

</details>

## 📁 Project Structure

```
📦 chatify
 ┣ 📂 feature
 ┃ ┣ 📂 auth          → Authentication module
 ┃ ┣ 📂 chat          → Messaging functionality
 ┃ ┗ 📂 home          → Home screen
 ┗ 📄 MainActivity.kt
```

**Clean Architecture Layers:**
```
📂 Each Feature Module
 ┣ 📂 data           → Repositories & Data Sources
 ┣ 📂 domain         → Business Logic & Models
 ┗ 📂 presentation   → UI (Fragments, ViewModels)
```

## 🚀 Getting Started

### Prerequisites

```
✅ Android Studio Koala or newer
✅ JDK 11
✅ Gradle 8.12.3+
```

### Setup

1️⃣ **Clone the repository**
```bash
git clone https://github.com/mrniteshray/ChatiFy.git
```

2️⃣ **Add Firebase Configuration**
- Place your `google-services.json` in the `app/` directory

4️⃣ **Sync & Build**
- Open project in Android Studio
- Sync Gradle files
- Run the app 🎉

### Build Commands

```bash
# Debug Build
./gradlew assembleDebug

# Release Build
./gradlew assembleRelease
```

---

<div align="center">

### 👨‍💻 Developed by [Nitesh Ray](https://github.com/mrniteshray)

⭐ Star this repo if you like it!

</div>
