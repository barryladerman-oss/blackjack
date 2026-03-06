# Complete Android Setup - Master Script
# This script automates the entire Android emulator setup process

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Blackjack Android Setup Automation" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$ErrorActionPreference = "Stop"

# Step 1: Install Android Studio
Write-Host "[Step 1/4] Installing Android Studio..." -ForegroundColor Yellow
Write-Host ""

$isAdmin = ([Security.Principal.WindowsPrincipal] [Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host "ERROR: This script requires Administrator privileges." -ForegroundColor Red
    Write-Host ""
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Right-click PowerShell" -ForegroundColor Cyan
    Write-Host "  2. Select 'Run as Administrator'" -ForegroundColor Cyan
    Write-Host "  3. Navigate to: $PWD" -ForegroundColor Cyan
    Write-Host "  4. Run: .\complete-setup.ps1" -ForegroundColor Cyan
    Write-Host ""
    pause
    exit 1
}

try {
    & .\install-android-studio.ps1
    if ($LASTEXITCODE -ne 0) { throw "Android Studio installation failed" }
}
catch {
    Write-Host ""
    Write-Host "Failed at Step 1. Please check errors above." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Waiting for Android Studio files to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 10

# Step 2: Setup Environment
Write-Host ""
Write-Host "[Step 2/4] Configuring environment variables..." -ForegroundColor Yellow
Write-Host ""

try {
    & .\setup-android-env.ps1
    if ($LASTEXITCODE -ne 0) { throw "Environment setup failed" }
}
catch {
    Write-Host ""
    Write-Host "Failed at Step 2. Continuing anyway..." -ForegroundColor Yellow
}

# Refresh environment for current session
$env:Path = [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")

# Step 3: Setup Emulator
Write-Host ""
Write-Host "[Step 3/4] Setting up Android emulator..." -ForegroundColor Yellow
Write-Host "This step requires Android Studio to complete its first-time setup." -ForegroundColor Cyan
Write-Host ""

$response = Read-Host "Have you opened and completed Android Studio setup? (y/n)"
if ($response -ne 'y') {
    Write-Host ""
    Write-Host "Please:" -ForegroundColor Yellow
    Write-Host "  1. Open Android Studio (search in Start menu)" -ForegroundColor Cyan
    Write-Host "  2. Complete the setup wizard (accept defaults)" -ForegroundColor Cyan
    Write-Host "  3. Wait for initial downloads to finish" -ForegroundColor Cyan
    Write-Host "  4. Then run: .\setup-emulator.ps1" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "After that, use:" -ForegroundColor Cyan
    Write-Host "  .\start-emulator.ps1  - to start the emulator" -ForegroundColor Yellow
    Write-Host "  .\deploy-app.ps1      - to deploy the Blackjack app" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 0
}

try {
    & .\setup-emulator.ps1
    if ($LASTEXITCODE -ne 0) { throw "Emulator setup failed" }
}
catch {
    Write-Host ""
    Write-Host "Failed at Step 3. You can try again later with: .\setup-emulator.ps1" -ForegroundColor Yellow
    exit 1
}

# Step 4: Start Emulator
Write-Host ""
Write-Host "[Step 4/4] Starting emulator..." -ForegroundColor Yellow
$response = Read-Host "Start the emulator now? (y/n)"
if ($response -eq 'y') {
    & .\start-emulator.ps1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Setup Complete!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Quick Reference:" -ForegroundColor Cyan
Write-Host "  .\start-emulator.ps1  - Start Android emulator" -ForegroundColor Yellow
Write-Host "  .\deploy-app.ps1      - Build and deploy app" -ForegroundColor Yellow
Write-Host ""
Write-Host "Don't forget to start your Blackjack server:" -ForegroundColor Yellow
Write-Host "  cd .." -ForegroundColor Cyan
Write-Host "  mvn spring-boot:run" -ForegroundColor Cyan
Write-Host ""
