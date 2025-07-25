# 🐾 TailsTale

<div align="center">
  <img src="https://img.shields.io/badge/Platform-Android-brightgreen" alt="Platform"/>
  <img src="https://img.shields.io/badge/Language-Kotlin-purple" alt="Language"/>
  <img src="https://img.shields.io/badge/Backend-Firebase-orange" alt="Backend"/>
  <img src="https://img.shields.io/badge/Status-In%20Development-yellow" alt="Status"/>
</div>

## 📱 Overview

**TailsTale** is an interactive and engaging mobile application that brings the nostalgic joy of virtual pet games to modern Android devices. Built with Kotlin and powered by Firebase, TailsTale creates an immersive pet care experience through video-enhanced interactions, real-time stat tracking, and habit-building gameplay mechanics.

Transform everyday pet care into a delightful digital adventure where users can adopt, name, feed, bathe, and nurture their virtual companions while monitoring health, happiness, and hunger levels in real-time.

## ✨ Features

### 🎮 Core Gameplay
- **Virtual Pet Simulation** - Adopt and care for your digital companion
- **Real-time Pet Stats** - Monitor health, happiness, and hunger levels
- **Interactive Care Activities** - Feed, bathe, play, and nurture your pet
- **Habit Building** - Turn pet care into positive daily routines

### 🎬 Enhanced User Experience
- **Video Overlays** - Dynamic video backgrounds instead of static icons
- **Clickable UI Elements** - Interactive buttons overlaid on pet videos
- **Story-driven Onboarding** - Immersive introduction to the pet world
- **Animated Transitions** - Smooth, engaging UI animations
- **Dark Mode Support** - Eye-friendly interface for all lighting conditions

### 🔔 Smart Features
- **Push Notifications** - Reminders for pet care activities
- **Real-time Data Sync** - Seamless experience across app sessions
- **Responsive Design** - Optimized for all Android screen sizes
- **Offline Capability** - Basic functionality available without internet

## 🛠️ Tech Stack

### Frontend
- **Language:** Kotlin
- **IDE:** Android Studio
- **UI Design:** Figma
- **3D Assets:** Blender
- **Architecture:** MVVM (Model-View-ViewModel)

### Backend & Services
- **Backend:** Firebase
  - Authentication
  - Firestore Database
  - Cloud Storage
  - Cloud Messaging (FCM)
  - Analytics
- **Real-time Sync:** Firebase Realtime Database

### Development Tools
- **Version Control:** Git & GitHub
- **Project Management:** Trello
- **Communication:** Discord
- **Testing:** Espresso & JUnit

## 🏗️ System Architecture

### App Structure
```
TailsTale/
├── app/
│   ├── src/main/java/com/tailstale/
│   │   ├── ui/
│   │   │   ├── home/          # Home screen with pet interaction
│   │   │   ├── stats/         # Pet statistics and analytics
│   │   │   ├── add/           # Add new pets or items
│   │   │   ├── activities/    # Pet activities and games
│   │   │   └── profile/       # User profile and settings
│   │   ├── data/
│   │   │   ├── models/        # Data models (Pet, User, etc.)
│   │   │   ├── repositories/  # Data layer abstraction
│   │   │   └── firebase/      # Firebase integration
│   │   ├── utils/             # Utility classes and helpers
│   │   └── viewmodels/        # ViewModels for MVVM
│   └── res/
│       ├── layout/            # XML layouts
│       ├── drawable/          # Images and vector assets
│       ├── values/            # Colors, strings, themes
│       └── raw/               # Video assets
```

### Navigation Flow
1. **Onboarding** → Story introduction → User signup/login
2. **Main App** → Bottom navigation with 5 core pages
3. **Real-time Updates** → Firebase sync for pet stats and user data

### Data Models
```kotlin
data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val health: Int,
    val happiness: Int,
    val hunger: Int,
    val lastFed: Timestamp,
    val adoptionDate: Timestamp
)

data class User(
    val uid: String,
    val username: String,
    val email: String,
    val pets: List<Pet>,
    val achievements: List<Achievement>
)
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Kotlin 1.8+
- Android SDK 24+
- Firebase project setup

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/tailstale.git
   cd tailstale
   ```

2. **Set up Firebase**
   - Create a new Firebase project
   - Add your Android app to the project
   - Download `google-services.json` and place it in `app/`
   - Enable Authentication, Firestore, and Cloud Messaging

3. **Open in Android Studio**
   - Open the project in Android Studio
   - Sync Gradle files
   - Build and run on your device/emulator

### Configuration

1. **Firebase Setup**
   ```kotlin
   // Add to app/build.gradle
   implementation 'com.google.firebase:firebase-auth:21.0.1'
   implementation 'com.google.firebase:firebase-firestore:24.0.1'
   implementation 'com.google.firebase:firebase-messaging:23.0.0'
   ```

2. **Video Assets**
   - Place video files in `res/raw/`
   - Ensure proper video compression for mobile optimization

## 📋 Core Pages

### 🏠 Home
- Live pet video with interactive overlay buttons
- Quick access to feeding, bathing, and play actions
- Real-time stat indicators (health, happiness, hunger)

### 📊 Stats
- Detailed pet analytics and progress tracking
- Historical data visualization
- Achievement system and milestones

### ➕ Add
- Adopt new pets
- Purchase pet accessories and items
- Unlock new features and content

### 🎯 Activities
- Mini-games and interactive activities
- Training sessions for pet development
- Social features and pet interactions

### 👤 Profile
- User account management
- App settings and preferences
- Dark mode toggle and notification controls

## 🔔 Notifications

TailsTale uses Firebase Cloud Messaging (FCM) to send timely reminders:
- **Feeding Reminders** - When hunger levels drop
- **Health Alerts** - When pet needs attention
- **Achievement Notifications** - When milestones are reached
- **Daily Check-ins** - Encouraging regular app usage

## 🎨 UI/UX Design

### Design Principles
- **Video-first Interface** - Dynamic backgrounds enhance immersion
- **Intuitive Navigation** - Clear, accessible bottom navigation
- **Responsive Layout** - Adapts to different screen sizes
- **Accessibility** - Support for screen readers and color contrast

### Dark Mode
Comprehensive dark theme support with:
- Automatic system theme detection
- Manual toggle in profile settings
- Optimized video contrast for dark environments

## 🤝 Contributing

We welcome contributions to TailsTale! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Development Guidelines
- Follow Kotlin coding conventions
- Write unit tests for new features
- Update documentation as needed
- Ensure UI responsiveness across devices




## 👥 Contributors

- **[Pratik Joshi, scrum Master, backend developer]**
- **[Prajwal Bhandari, Frontend developer]**
- **[Hemanta budhathoki, backend developer]**
- **[Sujal Bajracharya, UI/UX Design]**
- **[Rahul Kumar Sharma, tester]**   -



## 🙏 Acknowledgments

- Inspired by classic virtual pet games like Tamagotchi
- Firebase team for excellent backend services
- Android development community for continued support



---

<div align="center">
  <p>Made with ❤️ for virtual pet lovers everywhere</p>
  <p>⭐ Star this repo if you find it helpful!</p>
</div>
