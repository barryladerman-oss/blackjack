# Android Emulator Setup Script
# Creates and configures an Android Virtual Device (AVD)

Write-Host "=== Android Emulator Setup ===" -ForegroundColor Green
Write-Host ""

# Check if ANDROID_HOME is set
if (-not $env:ANDROID_HOME) {
    Write-Host "ERROR: ANDROID_HOME not set." -ForegroundColor Red
    Write-Host "Please run: .\setup-android-env.ps1 first" -ForegroundColor Yellow
    exit 1
}

if (-not (Test-Path $env:ANDROID_HOME)) {
    Write-Host "ERROR: Android SDK not found at: $env:ANDROID_HOME" -ForegroundColor Red
    exit 1
}

Write-Host "Android SDK: $env:ANDROID_HOME" -ForegroundColor Cyan
Write-Host ""

# Check for required tools
$sdkmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\sdkmanager.bat"
$avdmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat"
$emulator = "$env:ANDROID_HOME\emulator\emulator.exe"

# Use alternative paths if cmdline-tools not in 'latest'
if (-not (Test-Path $sdkmanager)) {
    $cmdlineToolsDir = Get-ChildItem -Path "$env:ANDROID_HOME\cmdline-tools" -Directory | Select-Object -First 1
    if ($cmdlineToolsDir) {
        $sdkmanager = "$($cmdlineToolsDir.FullName)\bin\sdkmanager.bat"
        $avdmanager = "$($cmdlineToolsDir.FullName)\bin\avdmanager.bat"
    }
}

if (-not (Test-Path $sdkmanager)) {
    Write-Host "ERROR: sdkmanager not found." -ForegroundColor Red
    Write-Host "Please open Android Studio and complete initial setup, or download command-line tools." -ForegroundColor Yellow
    exit 1
}

Write-Host "Installing required SDK components..." -ForegroundColor Cyan
Write-Host "This may take several minutes..." -ForegroundColor Yellow
Write-Host ""

# Accept licenses first
Write-Host "Accepting Android SDK licenses..." -ForegroundColor Cyan
$yes = "y" * 10  # Multiple 'y' responses for all licenses
$yes | & $sdkmanager --licenses 2>&1 | Out-Null

# Install essential components
$components = @(
    "platform-tools",
    "platforms;android-34",
    "system-images;android-34;google_apis;x86_64",
    "emulator",
    "build-tools;34.0.0"
)

foreach ($component in $components) {
    Write-Host "Installing $component..." -ForegroundColor Yellow
    & $sdkmanager $component
}

Write-Host ""
Write-Host "Creating Android Virtual Device (AVD)..." -ForegroundColor Cyan

# Check if AVD already exists
$avdName = "Pixel_5_API_34"
$avdList = & $avdmanager list avd
if ($avdList -match $avdName) {
    Write-Host "AVD '$avdName' already exists." -ForegroundColor Yellow
    $response = Read-Host "Delete and recreate? (y/n)"
    if ($response -eq 'y') {
        Write-Host "Deleting existing AVD..." -ForegroundColor Yellow
        & $avdmanager delete avd -n $avdName
    } else {
        Write-Host "Keeping existing AVD." -ForegroundColor Green
        $avdName = "Pixel_5_API_34_New"
        Write-Host "Creating new AVD with name: $avdName" -ForegroundColor Cyan
    }
}

# Create AVD
Write-Host "Creating AVD: $avdName" -ForegroundColor Cyan
$createCommand = "echo no | `"$avdmanager`" create avd -n $avdName -k `"system-images;android-34;google_apis;x86_64`" -d pixel_5"
Invoke-Expression $createCommand

Write-Host ""
Write-Host "=== Emulator Setup Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Available AVDs:" -ForegroundColor Cyan
& $emulator -list-avds

Write-Host ""
Write-Host "To launch the emulator:" -ForegroundColor Cyan
Write-Host "  .\start-emulator.ps1" -ForegroundColor Yellow
Write-Host ""
Write-Host "Or manually:" -ForegroundColor Cyan
Write-Host "  emulator -avd $avdName" -ForegroundColor Yellow
Write-Host ""
