# Android Emulator - Automated Setup Scripts

This directory contains PowerShell scripts to automate the Android emulator setup process.

## Quick Start

**Option 1: Fully Automated (Recommended)**

Run as Administrator in PowerShell:
```powershell
cd android-client
.\complete-setup.ps1
```

This will install Android Studio, configure environment variables, create an emulator, and optionally start it.

**Option 2: Step by Step**

Run each script in order:

```powershell
# 1. Install Android Studio (requires Administrator)
.\install-android-studio.ps1

# 2. Setup environment variables (restart terminal after this)
.\setup-android-env.ps1

# 3. Create emulator (requires Android Studio first-time setup to be complete)
.\setup-emulator.ps1

# 4. Start the emulator
.\start-emulator.ps1

# 5. Deploy the Blackjack app
.\deploy-app.ps1
```

## Script Descriptions

| Script | Purpose | Requires Admin |
|--------|---------|----------------|
| `complete-setup.ps1` | Runs all setup steps in sequence | Yes |
| `install-android-studio.ps1` | Installs Chocolatey and Android Studio | Yes |
| `setup-android-env.ps1` | Configures ANDROID_HOME and PATH variables | No* |
| `setup-emulator.ps1` | Installs SDK components and creates AVD | No |
| `start-emulator.ps1` | Launches the Android emulator | No |
| `deploy-app.ps1` | Builds and installs the Blackjack app | No |

*Run as Administrator for system-wide environment variables (recommended)

## Requirements

- Windows 10/11
- PowerShell 5.1 or later
- Internet connection
- ~5 GB free disk space

## Troubleshooting

### "Scripts are disabled on this system"
Run in PowerShell as Administrator:
```powershell
Set-ExecutionPolicy RemoteSigned -Scope CurrentUser
```

### "ANDROID_HOME not found"
1. Ensure Android Studio installation completed successfully
2. Run `.\setup-android-env.ps1` again
3. Restart your terminal/VS Code
4. Verify with: `$env:ANDROID_HOME`

### Emulator won't start
1. Enable Hyper-V (Windows) or Intel HAXM for hardware acceleration
2. Check virtualization is enabled in BIOS
3. Ensure at least 8GB RAM available

### App deployment fails
1. Ensure emulator is fully booted (shows home screen)
2. Check connection: `adb devices` (should show "emulator-5554")
3. Try: `adb kill-server` then `adb start-server`

### Cannot connect to server
The app connects to `http://10.0.2.2:8080` (special emulator localhost address).
Ensure your Spring Boot server is running:
```powershell
cd ..  # Return to project root
mvn spring-boot:run
```

## Manual Installation Alternative

If automated scripts fail, you can install manually:

1. Download Android Studio: https://developer.android.com/studio
2. Run installer with defaults
3. Open Android Studio → Complete setup wizard
4. Tools → Device Manager → Create Device
5. Run emulator from Device Manager

## Daily Usage

After initial setup, you only need:

```powershell
# Start emulator
.\start-emulator.ps1

# Deploy app (if code changed)
.\deploy-app.ps1
```

## Clean Up

To remove everything:
```powershell
# Uninstall Android Studio
choco uninstall androidstudio

# Remove SDK files
Remove-Item "$env:LOCALAPPDATA\Android" -Recurse -Force
```
