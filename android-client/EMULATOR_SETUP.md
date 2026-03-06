# Android Emulator Setup Guide

## Quick Setup with Android Studio (Recommended)

1. **Download and Install Android Studio**
   - Visit: https://developer.android.com/studio
   - Download the installer for Windows
   - Run the installer with default settings

2. **Set up an Android Virtual Device (AVD)**
   - Open Android Studio
   - Go to: Tools → Device Manager (or Tools → AVD Manager)
   - Click "Create Device"
   - Select a device definition (e.g., Pixel 5)
   - Select a system image (download if needed):
     - Recommended: API 33 (Android 13) or API 34 (Android 14)
   - Configure advanced settings if needed (RAM, storage)
   - Click "Finish"

3. **Launch the Emulator**
   - In Device Manager, click the ▶️ play button next to your AVD
   - Wait for emulator to boot (may take 1-2 minutes first time)

4. **Set Environment Variables** (for command-line access)
   ```powershell
   # Add to your PowerShell profile or set permanently via System Properties
   $env:ANDROID_HOME = "C:\Users\YourUsername\AppData\Local\Android\Sdk"
   $env:Path += ";$env:ANDROID_HOME\platform-tools"
   $env:Path += ";$env:ANDROID_HOME\emulator"
   ```

## Command-Line Setup (Advanced Users)

1. **Install Android Command Line Tools**
   - Download: https://developer.android.com/studio#command-tools
   - Extract to: `C:\Android\cmdline-tools\latest\`

2. **Set Environment Variables**
   ```powershell
   $env:ANDROID_HOME = "C:\Android"
   $env:Path += ";$env:ANDROID_HOME\cmdline-tools\latest\bin"
   $env:Path += ";$env:ANDROID_HOME\platform-tools"
   $env:Path += ";$env:ANDROID_HOME\emulator"
   ```

3. **Install SDK Components**
   ```powershell
   sdkmanager "platform-tools" "platforms;android-33" "emulator"
   sdkmanager "system-images;android-33;google_apis;x86_64"
   ```

4. **Create AVD**
   ```powershell
   avdmanager create avd -n Pixel5 -k "system-images;android-33;google_apis;x86_64" -d pixel_5
   ```

5. **Launch Emulator**
   ```powershell
   emulator -avd Pixel5
   ```

## Verify Installation

Once Android Studio is installed and you've set up ANDROID_HOME:

```powershell
# Verify ADB is accessible
adb version

# List available AVDs
emulator -list-avds

# Check running devices
adb devices
```

## Running the Blackjack App

After the emulator is running:

```powershell
cd android-client
.\gradlew installDebug
```

Or use Android Studio:
1. Open the `android-client` folder in Android Studio
2. Wait for Gradle sync
3. Click Run (▶️) to install and launch the app

## Troubleshooting

- **Emulator slow**: Enable hardware acceleration (Intel HAXM or Hyper-V)
- **ANDROID_HOME not found**: Set environment variables permanently via System Properties → Environment Variables
- **Build errors**: Ensure Java 17+ is installed: `java -version`
- **Connection to API**: Update API base URL in the app configuration to point to your server IP (not localhost when using emulator)
