# 📱 Installation Guide - Modern Music Player

## For Users (Installing the APK)

### Method 1: Direct Install on Android Device

#### Step 1: Enable Unknown Sources
1. Open **Settings** on your Android device
2. Go to **Security** or **Privacy**
3. Enable **Install from Unknown Sources** or **Allow from this source**
   - On Android 8+: Enable for your File Manager or Browser

#### Step 2: Transfer APK to Device
**Option A - USB Cable:**
1. Connect your Android device to computer via USB
2. Copy `app-debug.apk` from `app\build\outputs\apk\debug\`
3. Paste to device's **Downloads** folder

**Option B - Cloud Storage:**
1. Upload APK to Google Drive, Dropbox, or similar
2. Download on your Android device

**Option C - Direct WiFi (Android Studio):**
1. Connect device and computer to same WiFi
2. In Android Studio, click Run (▶️) button
3. Select your device from list

#### Step 3: Install APK
1. Open **File Manager** or **Downloads** app
2. Tap on **app-debug.apk**
3. Tap **Install**
4. Wait for installation to complete
5. Tap **Open** to launch the app

### Method 2: Install via ADB (Advanced)

#### Prerequisites:
- USB Debugging enabled on Android device
- ADB installed on computer

#### Steps:
```powershell
# Navigate to APK directory
cd "c:\Users\user\Desktop\music apk\app\build\outputs\apk\debug"

# Install APK
adb install app-debug.apk

# If device already has app installed
adb install -r app-debug.apk
```

---

## For Developers (Building from Source)

### Prerequisites Installation

#### 1. Install Java Development Kit (JDK 17)
```powershell
# Download from: https://www.oracle.com/java/technologies/downloads/#java17
# Or use OpenJDK: https://adoptium.net/

# Verify installation
java -version
```

#### 2. Install Android Studio
1. Download from: https://developer.android.com/studio
2. Run installer (default options)
3. Complete setup wizard
4. Install recommended SDK components

#### 3. Set up Android SDK
Android Studio will prompt you to:
- Install Android SDK
- Install SDK Build Tools
- Install Android Emulator (optional)

### Building the Project

#### Method 1: Android Studio (Recommended)

1. **Open Project**
   ```
   File → Open → Select: c:\Users\user\Desktop\music apk
   ```

2. **Wait for Gradle Sync**
   - First sync may take 5-10 minutes
   - Downloads all dependencies

3. **Configure Firebase**
   - Replace `google-services.json` with your Firebase config
   - See SETUP_GUIDE.md for details

4. **Build APK**
   ```
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   ```
   
5. **Find APK**
   - Click "locate" in build notification
   - Or navigate to: `app\build\outputs\apk\debug\app-debug.apk`

#### Method 2: Command Line

**Using PowerShell:**
```powershell
# Navigate to project directory
cd "c:\Users\user\Desktop\music apk"

# Clean previous builds
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# APK location: app\build\outputs\apk\debug\app-debug.apk
```

**Using Batch Script:**
```batch
# Double-click build.bat in project root
# Or run from command prompt:
build.bat
```

### Building Release APK (For Production)

#### 1. Generate Keystore
```powershell
keytool -genkey -v -keystore release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias release
```

**Save this information securely:**
- Keystore password
- Key alias
- Key password
- Keystore location

#### 2. Create signing config in app/build.gradle
```gradle
android {
    signingConfigs {
        release {
            storeFile file('../release-key.jks')
            storePassword 'your_store_password'
            keyAlias 'release'
            keyPassword 'your_key_password'
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

#### 3. Build Release APK
```powershell
.\gradlew assembleRelease
```

**APK Location:** `app\build\outputs\apk\release\app-release.apk`

---

## First Launch Setup

### 1. Create Firebase Project
1. Visit: https://console.firebase.google.com/
2. Create new project
3. Add Android app
4. Package name: `com.modernmusicplayer.app`
5. Download `google-services.json`
6. Place in: `app\` folder

### 2. Enable Firebase Features
- **Authentication:** Enable Email/Password
- **Firestore:** Create database in test mode
- **Storage:** (Optional) For user profile images

### 3. Test the App
1. Install APK on device
2. Launch app
3. Click "Sign Up"
4. Create test account:
   - Email: test@example.com
   - Password: test123
   - Name: Test User
5. Explore features

---

## Troubleshooting

### Installation Issues

**"App not installed"**
- Solution: Uninstall old version first
- Or use: `adb install -r app-debug.apk`

**"Parse error"**
- Solution: APK may be corrupted, rebuild
- Ensure device meets minimum requirements (Android 7.0+)

**"Installation blocked"**
- Solution: Enable "Install from Unknown Sources"
- Check device storage space

### Build Issues

**"SDK location not found"**
```
Solution: Android Studio creates local.properties automatically
Or create manually:
sdk.dir=C\:\\Users\\YourUser\\AppData\\Local\\Android\\Sdk
```

**"Java version mismatch"**
```
Solution: Install JDK 17
File → Project Structure → JDK location
```

**"Gradle sync failed"**
```
Solution: 
1. Check internet connection
2. Delete .gradle folder
3. File → Invalidate Caches / Restart
4. Sync again
```

**"Firebase configuration missing"**
```
Solution:
1. Ensure google-services.json in app/ folder
2. Package name matches Firebase console
3. Sync project with Gradle
```

### Runtime Issues

**"App crashes on launch"**
```
Check:
1. Firebase properly configured
2. Internet connection available
3. Permissions granted
4. Check Logcat for errors
```

**"Music won't play"**
```
Check:
1. Internet connection
2. Audio URLs are valid
3. Volume not muted
4. Audio focus permissions
```

**"Login fails"**
```
Check:
1. Firebase Authentication enabled
2. Email/Password method enabled
3. Correct Firebase configuration
4. Internet connection
```

---

## Distribution Options

### 1. Google Play Store
- Create developer account ($25 one-time)
- Generate signed AAB (not APK):
  ```
  .\gradlew bundleRelease
  ```
- Upload AAB to Play Console
- Complete store listing
- Submit for review

### 2. Direct Distribution
- Share APK file directly
- Host on website
- Use app distribution services (Firebase App Distribution, etc.)

### 3. Beta Testing
- Use Google Play Beta testing
- Firebase App Distribution
- TestFlight equivalent for Android

---

## Security Best Practices

### For Release Builds:

1. **Never commit:**
   - `google-services.json` with real credentials
   - Keystore files
   - Passwords or API keys

2. **Use environment variables:**
   ```gradle
   buildConfigField "String", "API_KEY", "\"${System.getenv('API_KEY')}\""
   ```

3. **Enable ProGuard/R8:**
   - Already configured in build.gradle
   - Obfuscates code
   - Reduces APK size

4. **Update regularly:**
   - Dependencies
   - Security patches
   - Firebase SDK

---

## System Requirements

### Minimum Device Requirements:
- **Android Version:** 7.0 (Nougat) or higher
- **API Level:** 24+
- **RAM:** 2GB minimum, 4GB recommended
- **Storage:** 100MB for app + cache space
- **Internet:** Required for streaming

### Development Requirements:
- **Windows:** 10/11 (64-bit)
- **RAM:** 8GB minimum, 16GB recommended
- **Storage:** 10GB free space (for Android Studio + SDK)
- **Java:** JDK 17
- **Internet:** Required for dependencies

---

## Quick Reference Commands

```powershell
# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Build release APK
.\gradlew assembleRelease

# Build AAB for Play Store
.\gradlew bundleRelease

# Install on connected device
.\gradlew installDebug

# Uninstall from device
adb uninstall com.modernmusicplayer.app

# View device logs
adb logcat

# Check connected devices
adb devices
```

---

## Support Resources

- **Documentation:** See README.md, SETUP_GUIDE.md
- **API Integration:** See API_INTEGRATION.md
- **Android Docs:** https://developer.android.com/
- **Firebase Docs:** https://firebase.google.com/docs
- **Kotlin Docs:** https://kotlinlang.org/docs/

---

**Happy Building! 🚀**

If you encounter any issues not covered here, check the detailed documentation files or search Stack Overflow for Android-specific solutions.
