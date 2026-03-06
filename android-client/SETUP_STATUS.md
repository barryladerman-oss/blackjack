# Android Emulator - Quick Setup Status

## ✅ Completed Steps

1. **SDK Command-line Tools** - Installed at `C:\Users\Barry\AppData\Local\Android\Sdk`
2. **Environment Variables** - ANDROID_HOME set to SDK location
3. **Licenses** - All Android SDK licenses accepted

## ⏳ In Progress / Remaining Steps

### Step 4: Install SDK Components

**Option A: Run the batch file (Simplest)**
```cmd
.\install-sdk-components.bat
```
This will download (~1.5 GB):
- Platform tools (ADB)
- Android 34 platform
- System image for emulator  
- Emulator executable
- Build tools

**Option B: Manual command**
```powershell
cd "$env:ANDROID_HOME\cmdline-tools\latest\bin"
.\sdkmanager.bat "platform-tools" "platforms;android-34" "system-images;android-34;google_apis;x86_64" "emulator" "build-tools;34.0.0"
```

### Step 5: Create Emulator (After Step 4 completes)
```powershell
cd "$env:ANDROID_HOME\cmdline-tools\latest\bin"
echo no | .\avdmanager.bat create avd -n Pixel_5_API_34 -k "system-images;android-34;google_apis;x86_64" -d pixel_5
```

### Step 6: Start Emulator
```powershell
cd "$env:ANDROID_HOME\emulator"
.\emulator.exe -avd Pixel_5_API_34
```

### Step 7: Deploy Blackjack App
```powershell
cd C:\workspace\blackjack\android-client
.\gradlew.bat installDebug
```

## Quick Commands

After initial setup is complete, daily usage:

```powershell
# Start emulator
cd $env:ANDROID_HOME\emulator
.\emulator.exe -avd Pixel_5_API_34

# Deploy app (in new terminal)
cd C:\workspace\blackjack\ android-client
.\gradlew.bat installDebug
```

## Troubleshooting

**If downloads keep getting interrupted:**
- Open SDK Manager in a dedicated terminal window
- Let it run without interruption
- Downloads are ~1.5 GB total

**Alternative: Use Android Studio GUI**
1. Download: https://developer.android.com/studio
2. Install with defaults
3. Open SDK Manager: Tools → SDK Manager
4. Install: Android 34, System Image x86_64, Emulator
5. Create AVD: Tools → Device Manager → Create Device

## Current Status Check

```powershell
# Check if components are installed
Test-Path "$env:ANDROID_HOME\platform-tools\adb.exe"
Test-Path "$env:ANDROID_HOME\emulator\emulator.exe"

# If both return True, proceed to Step 5 (Create Emulator)
# If False, complete Step 4 (Install SDK Components)
```
