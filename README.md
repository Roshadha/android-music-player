# Modern Music Player 🎵

A beautiful, modern Android music player application with user authentication, global music streaming capabilities, and a premium UI/UX experience inspired by Spotify and Apple Music.

## ✨ Features

### 🎨 Modern UI Design
- **Sleek gradient backgrounds** (purple to blue) with dark theme
- **Glassmorphism effects** on cards and buttons
- **Smooth animations** and transitions throughout
- **Large album artwork display** with rounded corners
- **Bottom navigation** with intuitive icons
- **Mini player** that persists across screens

### 🎵 Music Player
- **Full playback controls**: Play, Pause, Next, Previous
- **Progress bar** with draggable seek functionality
- **Volume slider** with mute/unmute toggle
- **Repeat modes**: Off, Repeat All, Repeat One
- **Shuffle mode** for randomized playback
- **Background playback** with notification controls
- **Queue management** and playlist support

### 👤 User Features
- **Firebase Authentication** (Email/Password)
- **Profile creation and management**
- **Favorite songs** functionality
- **Custom playlists**
- **Recently played** tracking
- **Cross-device sync** (via Firebase)

### 🌐 Music Library
- **Global music streaming** (demo songs included)
- **Search functionality** for songs, artists, albums
- **Trending songs** section
- **Genre-based browsing**
- **High-quality album artwork**
- Easily integrable with **Spotify API** or other music services

## 🛠️ Technical Stack

- **Language**: Kotlin
- **UI**: Material Design 3, View Binding
- **Architecture**: MVVM with Repository pattern
- **Music Playback**: Media3 (ExoPlayer)
- **Authentication**: Firebase Auth
- **Database**: Cloud Firestore
- **Image Loading**: Glide
- **Animations**: Lottie (optional)
- **Networking**: Retrofit, OkHttp
- **Async**: Kotlin Coroutines, Flow

## 📋 Prerequisites

Before building the app, ensure you have:

1. **Android Studio** (latest version recommended)
2. **JDK 17** or higher
3. **Android SDK** (API 24+)
4. **Firebase account** (for authentication and database)

## 🚀 Setup Instructions

### 1. Clone the Project

The project structure is already created in your workspace.

### 2. Configure Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or use an existing one
3. Add an Android app with package name: `com.modernmusicplayer.app`
4. Download the `google-services.json` file
5. Replace the placeholder `google-services.json` in `app/` directory
6. Enable **Email/Password** authentication in Firebase Console
7. Create a **Cloud Firestore** database

### 3. Set Up Firestore Database

Create these collections in Firestore:

#### Collection: `users`
```
users/
  {userId}/
    - uid: string
    - email: string
    - displayName: string
    - profileImageUrl: string
    - favoritesSongIds: array
    - createdAt: timestamp
```

#### Collection: `songs` (Optional - demo songs are included)
```
songs/
  {songId}/
    - id: string
    - title: string
    - artist: string
    - album: string
    - albumArtUrl: string
    - audioUrl: string
    - duration: number (milliseconds)
    - genre: string
    - releaseYear: number
```

### 4. Build the APK

#### Using Android Studio:
1. Open the project in Android Studio
2. Wait for Gradle sync to complete
3. Go to `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
4. APK will be generated in `app/build/outputs/apk/debug/`

#### Using Command Line:
```bash
# For Windows PowerShell:
cd "c:\Users\user\Desktop\music apk"
.\gradlew assembleDebug

# APK location: app\build\outputs\apk\debug\app-debug.apk
```

#### For Release Build:
```bash
.\gradlew assembleRelease
```

### 5. Generate Signed APK (For Production)

1. In Android Studio: `Build` → `Generate Signed Bundle / APK`
2. Select **APK** and click Next
3. Create or select a keystore
4. Fill in keystore details
5. Select **release** build variant
6. Click Finish

## 🎵 Adding Real Music Streaming

The app includes demo songs for testing. To integrate real music:

### Option 1: Spotify API
1. Register at [Spotify Developer Dashboard](https://developer.spotify.com/)
2. Create an app and get credentials
3. Implement Spotify SDK or Web API
4. Update `MusicRepository.kt` with Spotify integration

### Option 2: Deezer API
1. Register at [Deezer Developers](https://developers.deezer.com/)
2. Get API credentials
3. Implement Deezer API calls
4. Update repository layer

### Option 3: Your Own Backend
1. Create a backend server with music files
2. Implement REST API
3. Update `MusicRepository.kt` to call your API
4. Ensure proper audio streaming support

## 📱 App Structure

```
app/
├── data/
│   ├── model/          # Data models (Song, User, Playlist)
│   └── repository/     # Data layer (Auth, Music)
├── service/
│   ├── MusicPlayerService.kt    # Background music service
│   └── MusicPlayerManager.kt    # Player state management
└── ui/
    ├── auth/           # Login/Signup screens
    ├── home/           # Home feed
    ├── search/         # Search functionality
    ├── library/        # User library
    ├── profile/        # User profile
    ├── player/         # Full player screen
    └── adapters/       # RecyclerView adapters
```

## 🎨 Customization

### Change Color Scheme
Edit `res/values/colors.xml`:
```xml
<color name="purple_500">#8B5CF6</color>  <!-- Primary -->
<color name="blue_500">#3B82F6</color>    <!-- Secondary -->
<color name="bg_dark">#0F172A</color>     <!-- Background -->
```

### Modify Gradient
Edit `res/drawable/gradient_background.xml`:
```xml
<gradient
    android:startColor="#YOUR_COLOR"
    android:centerColor="#YOUR_COLOR"
    android:endColor="#YOUR_COLOR" />
```

### Update App Name/Icon
1. Change app name in `res/values/strings.xml`
2. Replace icons in `res/mipmap/` directories
3. Update `ic_launcher.xml` for adaptive icon

## 🔧 Troubleshooting

### Build Errors
- Ensure JDK 17 is configured
- Run `./gradlew clean` before building
- Check internet connection for dependencies

### Firebase Issues
- Verify `google-services.json` is in correct location
- Check package name matches Firebase console
- Ensure Firebase dependencies are latest versions

### Playback Issues
- Check internet permissions in manifest
- Verify audio URLs are accessible
- Test with demo songs first

## 📄 License

This project is open source and available under the MIT License.

## 🤝 Contributing

Contributions are welcome! Feel free to:
- Report bugs
- Suggest features
- Submit pull requests

## 📞 Support

For issues or questions:
- Open an issue on GitHub
- Check existing documentation
- Review Firebase console logs

## 🎉 Acknowledgments

- Material Design 3 guidelines
- Firebase for backend services
- ExoPlayer for audio playback
- Glide for image loading

---

**Enjoy your modern music player! 🎵✨**

Made with ❤️ using Kotlin and Jetpack libraries
