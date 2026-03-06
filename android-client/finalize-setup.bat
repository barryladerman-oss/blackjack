@echo off
echo ========================================
echo Finalizing Android Emulator Setup
echo ========================================
echo.

set ANDROID_HOME=%LOCALAPPDATA%\Android\Sdk

echo Checking installation...
if not exist "%ANDROID_HOME%\platform-tools\adb.exe" (
    echo ERROR: Platform tools not found!
    echo Please wait for SDK component installation to complete.
    pause
    exit /b 1
)

if not exist "%ANDROID_HOME%\emulator\emulator.exe" (
    echo ERROR: Emulator not found!
    echo Please wait for SDK component installation to complete.
    pause
    exit /b 1
)

echo [OK] ADB installed
echo [OK] Emulator installed
echo.

echo Creating Android Virtual Device...
cd /d "%ANDROID_HOME%\cmdline-tools\latest\bin"
echo no | call avdmanager.bat create avd -n Pixel_5_API_34 -k "system-images;android-34;google_apis;x86_64" -d pixel_5

if errorlevel 1 (
    echo.
    echo Note: AVD may already exist, continuing...
)

echo.
echo ========================================
echo Setup Complete!
echo ========================================
echo.
echo Your emulator is ready to use.
echo.
echo To start the emulator:
echo   1. Open a terminal
echo   2. Run: %ANDROID_HOME%\emulator\emulator.exe -avd Pixel_5_API_34
echo.
echo To deploy the Blackjack app:
echo   1. Start the emulator (above)
echo   2. Wait for it to fully boot
echo   3. Run: gradlew.bat installDebug
echo.
pause
