# 🚀 Build WITHOUT Android Studio or Firebase

## ✨ What Changed?

Your app now works **completely offline** with:
- ✅ **No Firebase required** - Uses local Room database
- ✅ **No Google services** - All data stored locally
- ✅ **Build with command line only** - No Android Studio needed
- ✅ **Local user accounts** - Secure password hashing
- ✅ **Encrypted storage** - Android Security Crypto library

---

## 📋 Prerequisites

### Required Software:

1. **Java Development Kit (JDK) 17**
   ```powershell
   # Download from: https://adoptium.net/
   # Or: https://www.oracle.com/java/technologies/downloads/#java17
   
   # Verify installation:
   java -version
   # Should show: openjdk version "17.x.x"
   ```

2. **Android Command Line Tools** (No Android Studio needed!)
   ```powershell
   # Download from: https://developer.android.com/studio#command-tools
   # Choose: "Command line tools only"
   # Extract to: C:\Android\cmdline-tools\latest\
   ```

3. **Set Environment Variables**
   ```powershell
   # Add to System Environment Variables:
   ANDROID_HOME = C:\Android
   JAVA_HOME = C:\Program Files\Java\jdk-17
   
   # Add to Path:
   %ANDROID_HOME%\cmdline-tools\latest\bin
   %ANDROID_HOME%\platform-tools
   %JAVA_HOME%\bin
   ```

4. **Install Android SDK Components**
   ```powershell
   # Open PowerShell as Administrator:
   
   sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
   
   # Accept licenses:
   sdkmanager --licenses
   ```

---

## 🔧 Quick Setup (5 Minutes)

### Step 1: Navigate to Project
```powershell
cd "c:\Users\user\Desktop\music apk"
```

### Step 2: Build APK
```powershell
# Clean previous builds
.\gradlew clean

# Build debug APK (for testing)
.\gradlew assembleDebug

# Build release APK (for distribution)
.\gradlew assembleRelease
```

### Step 3: Find Your APK
```
Debug APK:   app\build\outputs\apk\debug\app-debug.apk
Release APK: app\build\outputs\apk\release\app-release.apk
```

### Step 4: Install on Device
```powershell
# Connect Android device via USB
# Enable USB Debugging on device

# Install APK
adb install app\build\outputs\apk\debug\app-debug.apk

# Or install on specific device
adb -s DEVICE_ID install app\build\outputs\apk\debug\app-debug.apk
```

---

## 🎯 Alternative Build Methods

### Method 1: Using Provided Scripts

**Windows Batch:**
```batch
build.bat
```

**PowerShell:**
```powershell
.\build.ps1
```

### Method 2: One-Line Build
```powershell
cd "c:\Users\user\Desktop\music apk" ; .\gradlew assembleDebug
```

### Method 3: Build + Install
```powershell
.\gradlew installDebug
```

---

## 📱 No Android Studio? No Problem!

### Text Editor Options:

1. **Visual Studio Code** (Recommended)
   - Free and lightweight
   - Install "Kotlin" extension
   - Install "Android" extension
   - Download: https://code.visualstudio.com/

2. **Notepad++**
   - Simple and fast
   - Good for quick edits
   - Download: https://notepad-plus-plus.org/

3. **IntelliJ IDEA Community**
   - Free Kotlin IDE
   - More features than VS Code
   - Download: https://www.jetbrains.com/idea/download/

4. **Sublime Text**
   - Fast and elegant
   - Download: https://www.sublimetext.com/

### Editing Code:
```powershell
# Open in VS Code
code "c:\Users\user\Desktop\music apk"

# Or open specific file
code "app\src\main\java\com\modernmusicplayer\app\ui\MainActivity.kt"
```

---

## 🔑 How Authentication Works Now

### No Firebase - Uses Local Database

**User Signup:**
1. Enter email, password, display name
2. Password is hashed with SHA-256
3. User stored in local Room database
4. User ID saved in encrypted SharedPreferences

**User Login:**
1. Enter email and password
2. Password hashed and compared with stored hash
3. If match, user ID saved to preferences
4. User logged in!

**Data Storage:**
- 📦 **Room Database** - Users, songs, playlists
- 🔐 **Encrypted SharedPreferences** - Current user session
- 💾 **SQLite** - All data stored locally on device

**Security:**
- Passwords are **never stored in plain text**
- Uses **SHA-256 hashing**
- SharedPreferences **encrypted** with AES-256
- Data **sandboxed** per app (Android security)

---

## 🎵 Music Data Storage

### Demo Songs Included

The app comes with 8 demo songs that are:
- Automatically loaded on first launch
- Stored in local database
- Available offline
- Can be marked as favorites

### Adding Your Own Songs

**Option 1: Edit the Demo Songs**

Edit file: `app\src\main\java\com\modernmusicplayer\app\data\repository\MusicRepository.kt`

```kotlin
private fun getDemoSongs(): List<Song> {
    return listOf(
        Song(
            id = "1",
            title = "YOUR SONG TITLE",
            artist = "YOUR ARTIST",
            album = "YOUR ALBUM",
            albumArtUrl = "https://your-image-url.com/cover.jpg",
            audioUrl = "https://your-audio-url.com/song.mp3",
            duration = 240000, // milliseconds
            genre = "Pop",
            releaseYear = 2024
        ),
        // Add more songs...
    )
}
```

**Option 2: Use Online Music APIs**

See `API_INTEGRATION.md` for integrating:
- Spotify API
- Deezer API
- YouTube Music API
- Your own backend

---

## 🛠️ Troubleshooting

### "Java not found"
```powershell
# Install JDK 17
# Download: https://adoptium.net/
# Set JAVA_HOME environment variable
```

### "SDK location not found"
```powershell
# Create local.properties file in project root:
echo sdk.dir=C:\\Android > local.properties

# Or set ANDROID_HOME environment variable
```

### "Gradlew not recognized"
```powershell
# Make sure you're in project directory
cd "c:\Users\user\Desktop\music apk"

# Try with .\
.\gradlew assembleDebug
```

### "Permission denied" on gradlew
```powershell
# Windows doesn't need execute permissions
# Just run: .\gradlew assembleDebug
```

### Build fails with "Could not download..."
```powershell
# Check internet connection
# Try with VPN if behind firewall
# Clear Gradle cache:
.\gradlew clean --refresh-dependencies
```

### "SDK license not accepted"
```powershell
# Accept licenses
sdkmanager --licenses
# Type 'y' for each license
```

---

## 📦 Building Release APK

### Step 1: Generate Signing Key
```powershell
keytool -genkey -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

**Save this info securely:**
- Keystore password: `your_password`
- Key alias: `my-key-alias`  
- Key password: `your_key_password`

### Step 2: Sign Your APK

Create `app\keystore.properties`:
```properties
storePassword=your_password
keyPassword=your_key_password
keyAlias=my-key-alias
storeFile=../my-release-key.jks
```

Update `app\build.gradle`:
```gradle
android {
    // ... existing config ...
    
    signingConfigs {
        release {
            def keystorePropertiesFile = rootProject.file("app/keystore.properties")
            def keystoreProperties = new Properties()
            keystoreProperties.load(new FileInputStream(keystorePropertiesFile))
            
            storeFile file(keystoreProperties['storeFile'])
            storePassword keystoreProperties['storePassword']
            keyAlias keystoreProperties['keyAlias']
            keyPassword keystoreProperties['keyPassword']
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Step 3: Build Signed APK
```powershell
.\gradlew assembleRelease
```

**Output:** `app\build\outputs\apk\release\app-release.apk`

---

## 🚀 Deploying Your App

### Install on Multiple Devices

```powershell
# List connected devices
adb devices

# Install on all connected devices
adb -d install app\build\outputs\apk\debug\app-debug.apk

# Install on specific device
adb -s DEVICE_SERIAL install app\build\outputs\apk\debug\app-debug.apk
```

### Share APK File

1. **Direct Transfer:**
   - Copy APK to device via USB
   - Send via Bluetooth
   - Share via messaging apps

2. **Web Hosting:**
   - Upload to file hosting (Google Drive, Dropbox)
   - Share download link
   - Users download and install

3. **QR Code:**
   - Generate QR code for download link
   - Users scan and install

---

## 💡 No Google Play? Alternative Distribution

### 1. F-Droid
- Open source app repository
- Free distribution
- Submit: https://f-droid.org/docs/

### 2. Amazon Appstore
- Alternative to Google Play
- Available on Fire devices
- Submit: https://developer.amazon.com/apps-and-games

### 3. APKPure / APKMirror
- Third-party app stores
- Direct APK hosting
- Good for regions without Play Store

### 4. Your Own Website
- Host APK on your server
- Provide direct download link
- Add update notifications in app

---

## 📊 Build Verification

### Check APK Info:
```powershell
# View APK details
aapt dump badging app\build\outputs\apk\debug\app-debug.apk

# Check APK size
Get-Item app\build\outputs\apk\debug\app-debug.apk | Select-Object Name, Length

# Verify signing
jarsigner -verify -verbose -certs app\build\outputs\apk\release\app-release.apk
```

---

## 🎓 Learning Command Line Building

### Useful Gradle Tasks:
```powershell
# List all tasks
.\gradlew tasks

# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease

# Install debug on device
.\gradlew installDebug

# Uninstall from device
.\gradlew uninstallDebug

# Run tests
.\gradlew test

# Check dependencies
.\gradlew dependencies

# View build info
.\gradlew build --info
```

---

## 🌟 Advantages of This Setup

✅ **No Cloud Dependency** - Works completely offline
✅ **Privacy First** - All data stays on device  
✅ **No Firebase Costs** - Free forever
✅ **Faster Builds** - No network overhead
✅ **Full Control** - Own your data
✅ **Easy Backup** - Just export database
✅ **Portable** - Build anywhere with JDK
✅ **Lightweight** - Smaller APK size

---

## 📞 Need Help?

### Stuck? Try These:

1. **Read error messages carefully** - They usually tell you what's wrong
2. **Google the error** - Someone has likely solved it
3. **Check Stack Overflow** - Android community is huge
4. **Rebuild from scratch** - `.\gradlew clean assembleDebug`
5. **Check Java/SDK versions** - Must match requirements

### Useful Commands:
```powershell
# Check Java
java -version

# Check Android tools
sdkmanager --list_installed

# Check connected devices
adb devices

# View device logs
adb logcat
```

---

## 🎉 You're All Set!

Your app now builds **without any external dependencies**:
- ❌ No Firebase
- ❌ No Google Services
- ❌ No Android Studio required
- ✅ 100% local and offline
- ✅ Build with just command line
- ✅ Edit with any text editor

**Just run:**
```powershell
cd "c:\Users\user\Desktop\music apk"
.\gradlew assembleDebug
```

**And install:**
```powershell
adb install app\build\outputs\apk\debug\app-debug.apk
```

**That's it! 🚀**

---

*Updated: October 2025*
*Works on: Windows, Mac, Linux*
*Requires: JDK 17, Android SDK*
