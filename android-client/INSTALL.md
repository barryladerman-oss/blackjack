# Quick Setup: Run Android App

## 🚨 Android Studio Required

The Android app needs Android Studio to run in an emulator.

---

## Fastest Way to Get Started

### 1️⃣ Install Android Studio
Download: **https://developer.android.com/studio**

### 2️⃣ Open Project
- Launch Android Studio
- Click "Open"
- Select: `C:\workspace\blackjack\android-client\`

### 3️⃣ Create Emulator
- Click Device Manager (phone icon)
- Create Device → Pixel 6 → API 34
- Start emulator

### 4️⃣ Start Server
```bash
cd c:\workspace\blackjack
mvn spring-boot:run
```

### 5️⃣ Run App
- Click Run button (green ▶️) in Android Studio
- Wait for app to install
- Use URL: `http://10.0.2.2:8080/`
- Play blackjack! 🎰

---

## Alternative: Use Physical Device

1. Enable USB Debugging on phone
2. Connect via USB
3. Open project in Android Studio
4. Click Run and select your device
5. Use URL: `http://YOUR_PC_IP:8080/`

---

## What You Need

✅ Windows 10/11 (64-bit)  
✅ 8 GB RAM minimum (16 GB recommended)  
✅ 10 GB free disk space  
✅ Internet connection  

---

**Download Android Studio:**  
👉 https://developer.android.com/studio

**Detailed instructions:** See [SETUP_AND_RUN.md](SETUP_AND_RUN.md)
