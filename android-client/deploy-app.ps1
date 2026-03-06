# Deploy Blackjack App to Emulator

Write-Host "=== Deploying Blackjack App ===" -ForegroundColor Green
Write-Host ""

# Check if emulator is running
$adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
if (-not $env:ANDROID_HOME -or -not (Test-Path $adb)) {
    $adb = "adb"  # Try system PATH
}

Write-Host "Checking for connected devices..." -ForegroundColor Cyan
try {
    $devices = & $adb devices 2>&1
    Write-Host $devices
    
    if ($devices -notmatch "emulator|device") {
        Write-Host ""
        Write-Host "ERROR: No emulator or device connected." -ForegroundColor Red
        Write-Host "Please start the emulator first:" -ForegroundColor Yellow
        Write-Host "  .\start-emulator.ps1" -ForegroundColor Cyan
        exit 1
    }
}
catch {
    Write-Host "ERROR: ADB not found. Please ensure Android SDK is installed." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Building and installing app..." -ForegroundColor Cyan
Write-Host "This will take a few moments..." -ForegroundColor Yellow
Write-Host ""

# Check if we're in the android-client directory
if (-not (Test-Path ".\gradlew.bat")) {
    Write-Host "ERROR: Must be run from the android-client directory" -ForegroundColor Red
    exit 1
}

# Build and install
try {
    & .\gradlew.bat installDebug
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "=== App Deployed Successfully! ===" -ForegroundColor Green
        Write-Host ""
        Write-Host "The Blackjack app is now installed on your emulator." -ForegroundColor Cyan
        Write-Host "Look for 'Blackjack' in the app drawer and tap to launch." -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Note: Make sure your Blackjack server is running:" -ForegroundColor Yellow
        Write-Host "  1. Open a new terminal" -ForegroundColor Cyan
        Write-Host "  2. Navigate to the project root" -ForegroundColor Cyan
        Write-Host "  3. Run: mvn spring-boot:run" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "The app will connect to: http://10.0.2.2:8080" -ForegroundColor Yellow
        Write-Host "(This is the special IP for localhost from Android emulator)" -ForegroundColor Gray
        Write-Host ""
    }
    else {
        Write-Host ""
        Write-Host "ERROR: Build failed. Check the output above for details." -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host ""
    Write-Host "ERROR: Failed to build/deploy app: $_" -ForegroundColor Red
    exit 1
}
