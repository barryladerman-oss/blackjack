# Android Client Quick Start Guide

## Prerequisites

Before you begin, ensure you have:

1. **Android Studio** (latest version recommended)
   - Download from: https://developer.android.com/studio
   - Includes Android SDK and necessary build tools

2. **JDK 11 or higher**
   - Usually bundled with Android Studio
   - Or download from: https://adoptium.net/

3. **Blackjack Server Running**
   - The server must be running on your computer
   - Default: `http://localhost:8080`

## Option 1: Build and Run in Android Studio

### Step 1: Open Project
1. Launch Android Studio
2. Click "Open an Existing Project"
3. Navigate to: `blackjack/android-client/`
4. Click "OK"

### Step 2: Sync Gradle
- Android Studio will automatically sync Gradle
- Wait for the sync to complete (check bottom status bar)
- If prompted, install any missing SDK components

### Step 3: Start an Emulator
1. Click "Device Manager" in Android Studio (phone icon)
2. Click "Create Device" if you don't have one
3. Select a device (e.g., Pixel 6)
4. Select a system image (API 26+ required, API 34 recommended)
5. Click "Finish"
6. Start the emulator by clicking the play button

### Step 4: Run the App
1. Make sure the blackjack server is running on your computer
2. Click the "Run" button (green play icon) in Android Studio
3. Select your emulator or connected device
4. The app will build and install automatically

### Step 5: Connect to Server
- The default URL (`http://10.0.2.2:8080/`) should work for emulators
- Click "Connect" and then "New Game"

## Option 2: Build from Command Line

### Using Gradle Wrapper (Recommended)

**Windows:**
```powershell
cd android-client
.\gradlew assembleDebug
```

**Mac/Linux:**
```bash
cd android-client
./gradlew assembleDebug
```

The APK will be generated at:
```
android-client/app/build/outputs/apk/debug/app-debug.apk
```

### Install APK to Device

**Using ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Or drag and drop the APK to the emulator**

## Running on Physical Android Device

### Step 1: Enable Developer Options
1. Go to Settings > About Phone
2. Tap "Build Number" 7 times
3. Developer Options will now be available in Settings

### Step 2: Enable USB Debugging
1. Go to Settings > Developer Options
2. Enable "USB Debugging"

### Step 3: Connect Device
1. Connect your phone to your computer via USB
2. Accept the "Allow USB Debugging" prompt on your phone
3. Verify connection: `adb devices`

### Step 4: Find Your Computer's IP Address

**Windows:**
```powershell
ipconfig
```
Look for "IPv4 Address" (e.g., 192.168.1.100)

**Mac:**
```bash
ifconfig | grep "inet "
```

**Linux:**
```bash
ip addr show | grep "inet "
```

### Step 5: Configure Server URL in App
1. Open the app on your device
2. Change the server URL to your computer's IP:
   - Example: `http://192.168.1.100:8080/`
3. Click "Connect"

### Important Notes for Physical Devices
- Your phone and computer must be on the same Wi-Fi network
- Make sure your firewall allows connections on port 8080
- Some routers have AP isolation enabled - disable it if needed

## Troubleshooting

### "Cannot connect to server"
- **For Emulator**: Use `http://10.0.2.2:8080/`
- **For Physical Device**: Use your computer's local IP (e.g., `http://192.168.1.x:8080/`)
- Check that the server is running: open `http://localhost:8080` in your browser
- Verify firewall settings allow connections on port 8080

### Gradle sync fails
```bash
# Clean and rebuild
cd android-client
./gradlew clean
./gradlew build
```

### "SDK location not found"
Create a `local.properties` file in `android-client/` with:
```
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```
(Adjust path for your system)

### App crashes on launch
- Check Logcat in Android Studio for error messages
- Ensure minimum SDK (API 26) is met
- Try cleaning and rebuilding the project

### Network security error
- The app allows cleartext traffic (HTTP) for development
- This is configured in AndroidManifest.xml
- For production, use HTTPS

## Building Release APK

### Generate Keystore
```bash
keytool -genkey -v -keystore release.keystore -alias blackjack -keyalg RSA -keysize 2048 -validity 10000
```

### Configure Signing
Add to `app/build.gradle`:
```gradle
android {
    signingConfigs {
        release {
            storeFile file("release.keystore")
            storePassword "your-password"
            keyAlias "blackjack"
            keyPassword "your-password"
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

### Build Release
```bash
./gradlew assembleRelease
```

APK location: `app/build/outputs/apk/release/app-release.apk`

## Project Structure

```
android-client/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/blackjack/client/
│   │       │   ├── MainActivity.java          # Main game screen
│   │       │   ├── model/                     # Data models
│   │       │   │   ├── Card.java
│   │       │   │   ├── GameState.java
│   │       │   │   ├── PlayerHandState.java
│   │       │   │   └── DealerState.java
│   │       │   └── api/                       # API communication
│   │       │       ├── BlackjackApiService.java
│   │       │       └── ApiClient.java
│   │       ├── res/                           # Resources
│   │       │   ├── layout/                    # UI layouts
│   │       │   ├── values/                    # Strings, colors, themes
│   │       │   └── mipmap/                    # App icons
│   │       └── AndroidManifest.xml
│   └── build.gradle                           # App dependencies
├── gradle/
│   └── wrapper/
├── build.gradle                               # Project config
├── settings.gradle                            # Module settings
└── README.md                                  # Full documentation
```

## Next Steps

1. Explore the code in Android Studio
2. Try modifying the UI colors in `res/values/colors.xml`
3. Add support for multiple hands (modify `startNewGame()`)
4. Implement additional game features (insurance, double down, split)
5. Add animations for card dealing

## Resources

- [Android Developers Guide](https://developer.android.com/guide)
- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Material Design](https://material.io/develop/android)
- [Blackjack API Documentation](../README.md)

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review Android Studio Logcat for errors
3. Verify server is running and accessible
4. Check network connectivity
