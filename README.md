# BlueMeanie Axon Scanner v3.0 "APEX"

A cinematic, military-grade BLE Axon device scanner for Android with a dark purple/indigo tactical aesthetic.

## Features

- **Native Android BLE Scanner** - Uses `BluetoothLeScanner` API directly
- **Multi-factor Axon Detection** - OUI matching, device name patterns, service UUID fingerprinting
- **Animated Radar Display** - 5 styles: Tactical, Sonar, Pulse, Grid, Orbital
- **15 Themes** - Classic, Carbon, Titanium, Aurora, Monolith, Arctic, Midnight, Quantum, Nova, Glass, Inferno, Spectre, Ember, Phantom, Venom
- **Background Engine** - Neural Network, Particle Field, Matrix Rain, Aurora, Static Grid
- **Font Engine** - Customizable fonts for all UI sections
- **Alert System** - Sound effects, haptic vibrations, push notifications, fullscreen alerts
- **OSMDroid Heatmap** - Real OpenStreetMap integration with heat overlays
- **Foreground Service** - Background scanning with persistent notification
- **Telegram Integration** - Optional bot notifications
- **Rooted Features** - 10 advanced features for rooted devices
- **Shizuku Support** - Enhanced background scanning capabilities

## Requirements

- Android 8.0+ (API 26+)
- Android Studio Hedgehog or later
- Java 17

## Building

1. Open Android Studio
2. File → Open → Select the `android` folder
3. Wait for Gradle sync to complete
4. Run → Build APK

Or from command line:

```bash
cd android
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Permissions Required

- `BLUETOOTH_SCAN` - BLE device scanning
- `BLUETOOTH_CONNECT` - Connect to BLE devices
- `ACCESS_FINE_LOCATION` - Required for BLE scanning on Android
- `POST_NOTIFICATIONS` - Detection alerts
- `INTERNET` - Map tiles
- `FOREGROUND_SERVICE` - Background scanning
- `VIBRATE` - Haptic feedback

## Easter Eggs

Hidden throughout the app:
- `bluemeanie23` in binary, Morse code, hieroglyphs, and braille
- Tap the logo 5 times on the welcome screen
- Tap "SYSTEM GEAR" header 5 times
- Look for hidden text in various screens

## Project Structure

```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/bluemeanie/axonscanner/
│   │   │   ├── data/          # Data layer
│   │   │   ├── domain/        # Domain models
│   │   │   ├── presentation/  # UI layer
│   │   │   ├── service/       # Background services
│   │   │   ├── util/          # Utilities
│   │   │   └── di/            # Dependency injection
│   │   └── res/               # Resources
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## License

Private - bluemeanie23