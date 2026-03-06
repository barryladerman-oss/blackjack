# Android SDK Environment Setup Script
# Run this after Android Studio is installed

Write-Host "=== Android SDK Environment Setup ===" -ForegroundColor Green
Write-Host ""

# Check for Android SDK in common locations
$possibleSdkPaths = @(
    "$env:LOCALAPPDATA\Android\Sdk",
    "$env:APPDATA\Android\Sdk",
    "C:\Android\Sdk",
    "$env:ProgramFiles\Android\Android Studio\sdk"
)

$sdkPath = $null
foreach ($path in $possibleSdkPaths) {
    if (Test-Path $path) {
        $sdkPath = $path
        Write-Host "Found Android SDK at: $sdkPath" -ForegroundColor Green
        break
    }
}

if (-not $sdkPath) {
    Write-Host "ERROR: Android SDK not found in common locations." -ForegroundColor Red
    Write-Host "Please install Android Studio first by running: .\install-android-studio.ps1" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Or manually specify SDK path:" -ForegroundColor Cyan
    $customPath = Read-Host "Enter Android SDK path (or press Enter to exit)"
    if ($customPath -and (Test-Path $customPath)) {
        $sdkPath = $customPath
    } else {
        exit 1
    }
}

Write-Host ""
Write-Host "Setting up environment variables..." -ForegroundColor Cyan

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)

if ($isAdmin) {
    Write-Host "Setting SYSTEM environment variables (requires restart)..." -ForegroundColor Yellow
    [System.Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkPath, "Machine")
    
    $machinePath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
    $pathsToAdd = @(
        "$sdkPath\platform-tools",
        "$sdkPath\emulator",
        "$sdkPath\cmdline-tools\latest\bin"
    )
    
    foreach ($pathToAdd in $pathsToAdd) {
        if ($machinePath -notlike "*$pathToAdd*") {
            $machinePath += ";$pathToAdd"
        }
    }
    [System.Environment]::SetEnvironmentVariable("Path", $machinePath, "Machine")
    Write-Host "System environment variables set successfully!" -ForegroundColor Green
} else {
    Write-Host "Setting USER environment variables..." -ForegroundColor Yellow
    [System.Environment]::SetEnvironmentVariable("ANDROID_HOME", $sdkPath, "User")
    
    $userPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    $pathsToAdd = @(
        "$sdkPath\platform-tools",
        "$sdkPath\emulator",
        "$sdkPath\cmdline-tools\latest\bin"
    )
    
    foreach ($pathToAdd in $pathsToAdd) {
        if ($userPath -notlike "*$pathToAdd*") {
            $userPath += ";$pathToAdd"
        }
    }
    [System.Environment]::SetEnvironmentVariable("Path", $userPath, "User")
    Write-Host "User environment variables set successfully!" -ForegroundColor Green
    Write-Host "Note: Run as Administrator to set system-wide variables." -ForegroundColor Yellow
}

# Update current session
$env:ANDROID_HOME = $sdkPath
$env:Path += ";$sdkPath\platform-tools;$sdkPath\emulator;$sdkPath\cmdline-tools\latest\bin"

Write-Host ""
Write-Host "Verifying installation..." -ForegroundColor Cyan

# Check for ADB
if (Test-Path "$sdkPath\platform-tools\adb.exe") {
    Write-Host "✓ ADB found" -ForegroundColor Green
    & "$sdkPath\platform-tools\adb.exe" version
} else {
    Write-Host "✗ ADB not found - may need to install platform-tools" -ForegroundColor Yellow
}

Write-Host ""
# Check for emulator
if (Test-Path "$sdkPath\emulator\emulator.exe") {
    Write-Host "✓ Emulator found" -ForegroundColor Green
} else {
    Write-Host "✗ Emulator not found - may need to install via SDK Manager" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== Environment Setup Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "IMPORTANT: Restart PowerShell and VS Code to apply changes." -ForegroundColor Yellow
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Restart your terminal/VS Code"
Write-Host "2. Run: .\setup-emulator.ps1  (to create an Android Virtual Device)"
Write-Host ""
