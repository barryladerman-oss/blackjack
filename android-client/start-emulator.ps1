# Start Android Emulator Script

Write-Host "=== Starting Android Emulator ===" -ForegroundColor Green
Write-Host ""

if (-not $env:ANDROID_HOME) {
    Write-Host "ERROR: ANDROID_HOME not set." -ForegroundColor Red
    Write-Host "Please run: .\setup-android-env.ps1 first" -ForegroundColor Yellow
    exit 1
}

$emulator = "$env:ANDROID_HOME\emulator\emulator.exe"
if (-not (Test-Path $emulator)) {
    Write-Host "ERROR: Emulator not found at: $emulator" -ForegroundColor Red
    exit 1
}

# Get list of AVDs
$avdmanager = "$env:ANDROID_HOME\cmdline-tools\latest\bin\avdmanager.bat"
if (-not (Test-Path $avdmanager)) {
    $cmdlineToolsDir = Get-ChildItem -Path "$env:ANDROID_HOME\cmdline-tools" -Directory | Select-Object -First 1
    if ($cmdlineToolsDir) {
        $avdmanager = "$($cmdlineToolsDir.FullName)\bin\avdmanager.bat"
    }
}

Write-Host "Available AVDs:" -ForegroundColor Cyan
$avds = & $emulator -list-avds

if ($avds -is [array] -and $avds.Length -gt 0) {
    $avdToUse = $avds[0]
    Write-Host "  $avds" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Starting AVD: $avdToUse" -ForegroundColor Green
    Write-Host "Emulator will open in a new window..." -ForegroundColor Cyan
    Write-Host ""
    
    # Start emulator in background
    Start-Process -FilePath $emulator -ArgumentList @("-avd", $avdToUse) -WindowStyle Normal
    
    Write-Host "Waiting for emulator to boot..." -ForegroundColor Yellow
    Write-Host "This may take 1-2 minutes..." -ForegroundColor Cyan
    Write-Host ""
    
    # Wait for device to be online
    $adb = "$env:ANDROID_HOME\platform-tools\adb.exe"
    if (Test-Path $adb) {
        $timeout = 120
        $elapsed = 0
        while ($elapsed -lt $timeout) {
            Start-Sleep -Seconds 5
            $elapsed += 5
            $devices = & $adb devices
            if ($devices -match "emulator") {
                Write-Host ""
                Write-Host "✓ Emulator is online!" -ForegroundColor Green
                Write-Host ""
                Write-Host "Device info:" -ForegroundColor Cyan
                & $adb devices
                Write-Host ""
                Write-Host "To deploy the Blackjack app, run:" -ForegroundColor Cyan
                Write-Host "  .\deploy-app.ps1" -ForegroundColor Yellow
                Write-Host ""
                exit 0
            }
            Write-Host "." -NoNewline
        }
        Write-Host ""
        Write-Host "Emulator is starting (check the emulator window)..." -ForegroundColor Yellow
    }
} elseif ($avds) {
    Write-Host "  $avds" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Starting AVD: $avds" -ForegroundColor Green
    Start-Process -FilePath $emulator -ArgumentList @("-avd", $avds) -WindowStyle Normal
    Write-Host "Emulator will open in a new window..." -ForegroundColor Cyan
} else {
    Write-Host "No AVDs found!" -ForegroundColor Red
    Write-Host "Please run: .\setup-emulator.ps1 to create one" -ForegroundColor Yellow
    exit 1
}
