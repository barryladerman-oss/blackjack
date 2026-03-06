# Android App Setup and Launch Guide

## ⚠️ Android Studio Required

To run the Android app in an emulator, you need Android Studio installed.

---

## Option 1: Install Android Studio (Recommended)

### Step 1: Download Android Studio

1. Visit: https://developer.android.com/studio
2. Download the latest version for Windows
3. Run the installer (android-studio-xxxx.exe)

### Step 2: Install Android Studio

1. Run the installer
2. Choose "Standard" installation
3. Accept all licenses
4. Wait for the SDK and emulator to download (this may take 15-30 minutes)

### Step 3: Open the Project

1. Launch Android Studio
2. Click **"Open"** (not "New Project")
3. Navigate to: `C:\workspace\blackjack\android-client\`
4. Click **"OK"**
5. Wait for Gradle sync to complete (watch the status bar at bottom)

### Step 4: Create/Start an Emulator

#### If you need to create an emulator:
1. Click **Device Manager** (phone icon in toolbar)
2. Click **"Create Device"**
3. Select a device (recommended: **Pixel 6**)
4. Click **"Next"**
5. Select a system image (recommended: **API 34** - Android 14)
6. Click **"Next"**, then **"Finish"**

#### Start the emulator:
1. In Device Manager, find your emulator
2. Click the **play button** (▶️) to start it
3. Wait for the emulator to boot (first boot takes 2-3 minutes)

### Step 5: Run the App

1. Make sure the blackjack server is running:
   ```bash
   cd c:\workspace\blackjack
   mvn spring-boot:run
   ```

2. In Android Studio, click the **Run button** (green play icon ▶️)
3. Select your emulator from the device list
4. Click **"OK"**
5. Wait for the app to build and install (first build takes 2-5 minutes)

### Step 6: Use the App

1. The app will launch automatically
2. Default server URL should be: `http://10.0.2.2:8080/`
3. Click **"Connect"**
4. Click **"New Game"**
5. Play blackjack! 🎰

---

## Option 2: Command-Line Tools Only (Advanced)

If you prefer not to install the full Android Studio, you can use command-line tools:

### Step 1: Install Android Command Line Tools

1. Download from: https://developer.android.com/studio#command-tools
2. Extract to: `C:\Android\cmdline-tools\`
3. Set environment variable:
   ```powershell
   $env:ANDROID_HOME = "C:\Android"
   [Environment]::SetEnvironmentVariable("ANDROID_HOME", "C:\Android", "User")
   ```

### Step 2: Install Required Components

```powershell
cd C:\Android\cmdline-tools\bin
.\sdkmanager.bat "platform-tools" "platforms;android-34" "build-tools;34.0.0" "emulator" "system-images;android-34;google_apis;x86_64"
```

### Step 3: Create an Emulator

```powershell
.\avdmanager.bat create avd -n Pixel6 -k "system-images;android-34;google_apis;x86_64" -d "pixel_6"
```

### Step 4: Start Emulator

```powershell
C:\Android\emulator\emulator.exe -avd Pixel6
```

### Step 5: Build and Install App

```powershell
cd C:\workspace\blackjack\android-client
.\gradlew.bat assembleDebug installDebug
```

---

## Option 3: Use a Physical Android Device

### Requirements
- Android device (Android 8.0 or higher)
- USB cable
- Same Wi-Fi network as your computer

### Steps

1. **Enable Developer Options on your phone:**
   - Go to Settings → About Phone
   - Tap "Build Number" 7 times
   - Go back to Settings → Developer Options
   - Enable "USB Debugging"

2. **Connect your device:**
   - Connect phone to computer via USB
   - Accept "Allow USB Debugging" prompt on phone

3. **Find your computer's IP address:**
   ```powershell
   ipconfig
   ```
   Look for "IPv4 Address" (e.g., 192.168.1.100)

4. **Open project in Android Studio:**
   - Open Android Studio
   - Open: `C:\workspace\blackjack\android-client\`
   - Wait for Gradle sync

5. **Run the app:**
   - Click Run (green play ▶️)
   - Select your physical device
   - Wait for installation

6. **Configure server URL in the app:**
   - Change URL to: `http://[YOUR_IP]:8080/`
   - Example: `http://192.168.1.100:8080/`
   - Click "Connect"
   - Start playing!

---

## Quick Start Checklist

- [ ] Android Studio installed
- [ ] Android SDK downloaded
- [ ] Emulator created (or physical device connected)
- [ ] Blackjack server running (`mvn spring-boot:run`)
- [ ] Project opened in Android Studio
- [ ] Gradle sync completed
- [ ] App running on device/emulator
- [ ] Server URL configured
- [ ] Playing blackjack! 🎰♠️♥️♦️♣️

---

## Troubleshooting

### "Gradle sync failed"
- Click **File → Invalidate Caches → Invalidate and Restart**
- Make sure you have internet connection for downloading dependencies

### "Emulator won't start"
- Check if virtualization is enabled in BIOS (Intel VT-x or AMD-V)
- Allocate more RAM to emulator (Settings → AVD Manager → Edit → Advanced)
- Try a different system image (API 30 instead of API 34)

### "Cannot connect to server"
- Verify server is running: open http://localhost:8080 in browser
- For emulator, use: `http://10.0.2.2:8080/`
- For physical device, use your PC's IP: `http://192.168.1.x:8080/`
- Check Windows Firewall settings

### "App crashes on launch"
- Check Logcat in Android Studio for error messages
- Ensure minimum SDK version (API 26) is met
- Clean and rebuild: Build → Clean Project, then Build → Rebuild Project

---

## System Requirements

### Minimum Requirements
- **OS:** Windows 10/11 (64-bit)
- **RAM:** 8 GB (16 GB recommended)
- **Disk Space:** 10 GB free space
- **CPU:** Intel/AMD processor with virtualization support

### Recommended for Best Performance
- **RAM:** 16 GB or more
- **Disk:** SSD (solid state drive)
- **CPU:** Multi-core processor
- **Internet:** For downloading SDK components

---

## Next Steps After Installation

1. **Complete Android Studio Setup**
   - Install recommended SDK components
   - Configure emulator hardware acceleration
   - Accept all Android SDK licenses

2. **Open the Project**
   - File → Open → `C:\workspace\blackjack\android-client\`

3. **Test the App**
   - Ensure server is running
   - Click Run
   - Start playing blackjack!

---

## Support Resources

- **Android Studio Docs:** https://developer.android.com/studio/intro
- **Emulator Setup:** https://developer.android.com/studio/run/emulator
- **Device Setup:** https://developer.android.com/studio/run/device
- **Gradle Issues:** https://developer.android.com/studio/build/dependencies

---

**Ready to install? Download Android Studio now:**  
👉 https://developer.android.com/studio

Once installed, open the `android-client` folder and click Run! 🚀
