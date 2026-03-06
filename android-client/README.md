# Blackjack Android Client

An Android mobile application client for the Blackjack REST API server.

## Features

- Connect to any Blackjack server instance
- Start new games with configurable number of hands
- Real-time game state updates
- Card counting information (running count, true count)
- Basic strategy recommendations
- Beautiful card-style UI with blackjack table theme

## Requirements

- Android SDK 26+ (Android 8.0 Oreo or higher)
- Java 8 or higher
- Gradle 8.0+

## Setup

1. **Install Android Studio**
   - Download from: https://developer.android.com/studio
   - Install with Android SDK

2. **Open Project**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `android-client` directory

3. **Configure Server URL**
   - The default URL is `http://10.0.2.2:8080/` (for Android emulator connecting to localhost)
   - For physical devices, use your computer's IP address (e.g., `http://192.168.1.100:8080/`)
   - You can change this in the app's settings field

4. **Build and Run**
   - Click the "Run" button in Android Studio
   - Select an emulator or connected device
   - The app will build and install automatically

## Running on Emulator

The default server URL (`http://10.0.2.2:8080/`) is specifically for Android emulators:
- `10.0.2.2` is a special alias to your host machine's `localhost`
- Start the blackjack server on your computer first
- Then run the Android app in the emulator

## Running on Physical Device

1. Find your computer's local IP address:
   - Windows: `ipconfig` (look for IPv4 Address)
   - Mac/Linux: `ifconfig` or `ip addr`

2. Make sure your device is on the same network as your computer

3. Update the server URL in the app to use your computer's IP:
   - Example: `http://192.168.1.100:8080/`

4. Make sure the blackjack server is running and accessible

## Usage

1. **Connect to Server**
   - Enter the server URL in the text field
   - Click "Connect"

2. **Start a Game**
   - Click "New Game" to start a new blackjack game
   - The game will start with 1 player hand

3. **Play**
   - Click "Hit" to draw another card for the current hand
   - Click "Stand" to end your turn on the current hand
   - The app shows recommendations based on basic strategy

4. **Game Information**
   - View running count and true count for card counting
   - See net units won/lost
   - Check cards remaining in the shoe

## Architecture

### Packages

- `com.blackjack.client` - Main application and activities
- `com.blackjack.client.model` - Data models matching API responses
- `com.blackjack.client.api` - Retrofit API service and client

### Key Libraries

- **Retrofit 2.9.0** - HTTP client for API calls
- **Gson 2.10.1** - JSON serialization/deserialization
- **Material Components** - Modern UI components
- **CardView** - Card-style layouts
- **ConstraintLayout** - Flexible layouts

## API Endpoints Used

- `POST /api/blackjack/start?hands={n}` - Start new game
- `GET /api/blackjack/state/{id}` - Get game state
- `POST /api/blackjack/hit/{id}?hand={n}` - Hit a hand
- `POST /api/blackjack/stand/{id}?hand={n}` - Stand a hand

## Building APK

To build a release APK:

```bash
cd android-client
./gradlew assembleRelease
```

The APK will be in: `app/build/outputs/apk/release/`

## Troubleshooting

### Cannot connect to server
- Check that the server is running
- Verify the URL is correct
- For emulator: use `http://10.0.2.2:8080/`
- For physical device: use your computer's local IP address
- Check firewall settings on your computer

### Build errors
- Make sure you have Android SDK installed
- Sync Gradle files in Android Studio
- Clean and rebuild the project

### UI not updating
- Check logcat for network errors
- Verify API responses are successful
- Check internet permissions in manifest

## License

This project is part of the Blackjack Server application.
