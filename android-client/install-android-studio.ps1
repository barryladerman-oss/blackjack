# Android Studio Installation Script
# Run this script as Administrator

Write-Host "=== Android Studio Automated Installation ===" -ForegroundColor Green
Write-Host ""

# Check if running as Administrator
$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: This script requires Administrator privileges." -ForegroundColor Red
    Write-Host "Please run PowerShell as Administrator and try again." -ForegroundColor Yellow
    exit 1
}

# Install Chocolatey if not present
Write-Host "Checking for Chocolatey..." -ForegroundColor Cyan
if (-not (Get-Command choco -ErrorAction SilentlyContinue)) {
    Write-Host "Installing Chocolatey package manager..." -ForegroundColor Yellow
    Set-ExecutionPolicy Bypass -Scope Process -Force
    [System.Net.ServicePointManager]::SecurityProtocol = [System.Net.ServicePointManager]::SecurityProtocol -bor 3072
    try {
        Invoke-Expression ((New-Object System.Net.WebClient).DownloadString('https://community.chocolatey.org/install.ps1'))
        Write-Host "Chocolatey installed successfully!" -ForegroundColor Green
    } catch {
        Write-Host "ERROR: Failed to install Chocolatey: $_" -ForegroundColor Red
        Write-Host "Please install manually from: https://chocolatey.org/install" -ForegroundColor Yellow
        exit 1
    }
    
    # Refresh environment
    $env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
} else {
    Write-Host "Chocolatey is already installed." -ForegroundColor Green
}

Write-Host ""
Write-Host "Installing Android Studio via Chocolatey..." -ForegroundColor Cyan
Write-Host "This may take 10-15 minutes depending on your internet connection..." -ForegroundColor Yellow
Write-Host ""

try {
    choco install androidstudio -y
    Write-Host ""
    Write-Host "Android Studio installed successfully!" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Failed to install Android Studio: $_" -ForegroundColor Red
    Write-Host "You can install manually from: https://developer.android.com/studio" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "=== Installation Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Cyan
Write-Host "1. Close and reopen PowerShell (or restart VS Code) to refresh environment variables"
Write-Host "2. Run: .\setup-android-env.ps1  (to configure environment variables)"
Write-Host "3. Run: .\setup-emulator.ps1     (to create and configure an emulator)"
Write-Host ""
Write-Host "Or launch Android Studio manually to complete setup via GUI." -ForegroundColor Yellow
