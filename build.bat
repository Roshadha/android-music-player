@echo off
echo ========================================
echo Modern Music Player - Quick Build Script
echo ========================================
echo.

echo Checking Gradle wrapper...
if not exist gradlew.bat (
    echo ERROR: gradlew.bat not found!
    echo Please ensure you are in the project root directory.
    pause
    exit /b 1
)

echo.
echo Starting build process...
echo This may take 5-10 minutes on first run.
echo.

call gradlew.bat clean assembleDebug

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location: app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo You can now install this APK on your Android device.
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo.
    echo Please check the error messages above.
    echo Common solutions:
    echo 1. Ensure you have Android SDK installed
    echo 2. Check your internet connection
    echo 3. Try running: gradlew clean
    echo 4. Open project in Android Studio for better error messages
    echo.
)

pause
