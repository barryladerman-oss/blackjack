@echo off
echo Installing Android SDK components...
echo This will take 3-5 minutes...
echo.

cd /d "%LOCALAPPDATA%\Android\Sdk\cmdline-tools\latest\bin"

call sdkmanager.bat "platform-tools" "platforms;android-34" "system-images;android-34;google_apis;x86_64" "emulator" "build-tools;34.0.0"

echo.
echo Installation complete!
pause
