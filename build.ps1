# Modern Music Player - Quick Build Script for PowerShell

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Modern Music Player - Quick Build Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Check if gradlew exists
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "ERROR: gradlew.bat not found!" -ForegroundColor Red
    Write-Host "Please ensure you are in the project root directory." -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}

Write-Host "Starting build process..." -ForegroundColor Yellow
Write-Host "This may take 5-10 minutes on first run." -ForegroundColor Yellow
Write-Host ""

# Run gradle build
& .\gradlew.bat clean assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK Location: app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "You can now install this APK on your Android device." -ForegroundColor Green
    Write-Host ""
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Please check the error messages above." -ForegroundColor Yellow
    Write-Host "Common solutions:" -ForegroundColor Yellow
    Write-Host "1. Ensure you have Android SDK installed" -ForegroundColor White
    Write-Host "2. Check your internet connection" -ForegroundColor White
    Write-Host "3. Try running: .\gradlew clean" -ForegroundColor White
    Write-Host "4. Open project in Android Studio for better error messages" -ForegroundColor White
    Write-Host ""
}

Read-Host "Press Enter to exit"
