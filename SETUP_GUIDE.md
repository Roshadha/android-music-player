# Modern Music Player - Setup Guide

## Quick Start

### Step 1: Install Android Studio
Download and install the latest version of Android Studio from:
https://developer.android.com/studio

### Step 2: Open the Project
1. Launch Android Studio
2. Click "Open" and navigate to: `c:\Users\user\Desktop\music apk`
3. Wait for Gradle sync to complete (may take a few minutes on first run)

### Step 3: Set Up Firebase

#### Create Firebase Project:
1. Visit https://console.firebase.google.com/
2. Click "Add project" or use existing project
3. Follow the setup wizard

#### Add Android App:
1. Click "Add app" → Select Android icon
2. Enter package name: `com.modernmusicplayer.app`
3. Download `google-services.json`
4. Place it in: `c:\Users\user\Desktop\music apk\app\`
   (Replace the existing placeholder file)

#### Enable Authentication:
1. In Firebase Console, go to "Authentication"
2. Click "Get Started"
3. Enable "Email/Password" sign-in method

#### Create Firestore Database:
1. In Firebase Console, go to "Firestore Database"
2. Click "Create database"
3. Start in "Test mode" (for development)
4. Choose a location close to you

### Step 4: Build the App

#### Option A: Using Android Studio
1. Click the green "Play" button (Run)
2. Select a connected device or emulator
3. Wait for the app to build and install

#### Option B: Build APK Only
1. Go to `Build` menu
2. Select `Build Bundle(s) / APK(s)` → `Build APK(s)`
3. APK location: `app\build\outputs\apk\debug\app-debug.apk`

#### Option C: Using Command Line
Open PowerShell in project directory:
```powershell
cd "c:\Users\user\Desktop\music apk"
.\gradlew assembleDebug
```

## Building Release APK (For Distribution)

### Generate Keystore:
```powershell
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

### Sign APK in Android Studio:
1. `Build` → `Generate Signed Bundle / APK`
2. Select `APK`
3. Create new or choose existing keystore
4. Fill in credentials (SAVE THESE SECURELY!)
5. Select `release` variant
6. Click `Finish`

## Common Issues & Solutions

### Issue: "google-services.json not found"
**Solution**: Download from Firebase Console and place in `app/` folder

### Issue: "Firebase initialization failed"
**Solution**: Ensure package name matches in Firebase Console and `build.gradle`

### Issue: "Cannot resolve symbol 'databinding'"
**Solution**: Run `Build` → `Clean Project`, then `Rebuild Project`

### Issue: Gradle sync fails
**Solution**: 
1. Check internet connection
2. Use VPN if behind firewall
3. Update Gradle wrapper: `gradle wrapper --gradle-version 8.2`

### Issue: "SDK location not found"
**Solution**: Create `local.properties` with:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```

## Testing the App

### Create Test Account:
1. Run the app
2. Click "Sign Up"
3. Enter email, password, and display name
4. Sign up and you'll be logged in

### Test Music Playback:
1. Navigate to Home screen
2. Tap any song from the trending list
3. Music should start playing
4. Mini player appears at bottom
5. Tap mini player to open full player

### Test Features:
- Play/Pause controls
- Next/Previous tracks
- Progress bar seeking
- Volume control
- Shuffle and Repeat modes
- Bottom navigation

## Adding Your Own Music

### Option 1: Use Firestore (Small Scale)
1. Go to Firebase Console → Firestore
2. Create collection: `songs`
3. Add documents with fields:
   - id: string
   - title: string
   - artist: string
   - audioUrl: string (hosted audio file URL)
   - albumArtUrl: string (image URL)
   - duration: number (milliseconds)

### Option 2: Integrate Spotify API
1. Register at: https://developer.spotify.com/
2. Create an app
3. Get Client ID and Secret
4. Modify `MusicRepository.kt` to use Spotify SDK

### Option 3: Build Your Own Backend
1. Create REST API with music endpoints
2. Host audio files on CDN
3. Update `MusicRepository.kt` to call your API

## App Customization

### Change App Name:
Edit `app\src\main\res\values\strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Change App Icon:
1. Right-click `res` folder
2. New → Image Asset
3. Follow wizard to generate icons
4. Replace launcher icons

### Change Colors:
Edit `app\src\main\res\values\colors.xml`:
```xml
<color name="purple_500">#YourColor</color>
<color name="blue_500">#YourColor</color>
```

### Modify Package Name (Advanced):
1. Right-click package in Android Studio
2. Refactor → Rename
3. Update in `build.gradle` and `AndroidManifest.xml`
4. Update in Firebase Console
5. Download new `google-services.json`

## Production Checklist

Before releasing your app:

- [ ] Replace all demo content
- [ ] Add privacy policy URL
- [ ] Implement proper error handling
- [ ] Add analytics (Firebase Analytics)
- [ ] Test on multiple devices
- [ ] Optimize images and resources
- [ ] Enable ProGuard/R8 for code shrinking
- [ ] Set proper versioning in `build.gradle`
- [ ] Test in-app permissions thoroughly
- [ ] Create app listing assets (screenshots, descriptions)
- [ ] Sign with release keystore
- [ ] Test signed release APK

## Deployment

### Google Play Store:
1. Create Developer account ($25 one-time fee)
2. Create app listing
3. Upload signed AAB (not APK)
4. Fill out store listing details
5. Submit for review

### Generate AAB for Play Store:
```powershell
.\gradlew bundleRelease
```
Location: `app\build\outputs\bundle\release\app-release.aab`

## Getting Help

- Check README.md for detailed documentation
- Review Firebase documentation: https://firebase.google.com/docs
- Android Developer Guide: https://developer.android.com/guide
- Stack Overflow for specific issues

## Performance Tips

1. **Enable R8**: Already enabled in `build.gradle`
2. **Optimize Images**: Use WebP format for smaller size
3. **Lazy Loading**: Implement pagination for large lists
4. **Cache Strategy**: Glide handles image caching automatically
5. **Background Tasks**: Use WorkManager for offline sync

---

**You're all set! Build something amazing! 🚀**
