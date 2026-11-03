# JustRow

**JustRow** is an Android rowing-workout tracker built as a Year 3 Mobile Application Development project. It provides rowing-session metrics, custom distance workouts, account registration and sign-in, and cloud-backed workout history.

> **Project status:** Educational project / prototype. GPS and motion-sensor estimates can vary by device and conditions; do not rely on the app for safety-critical or professional athletic measurements.

## Features

- **Account registration and sign-in** with Firebase Authentication using email and password.
- **Standard rowing sessions** with live elapsed time, GPS distance, current and average split pace per 500 metres, and estimated strokes per minute (SPM).
- **Custom-distance workouts** where a target distance can be entered before starting a session.
- **Workout saving** to the signed-in user’s Firestore data.
- **Session history** displayed in the app, with a detail view for saved workout information.
- **Workout sharing** through Android’s share menu.
- **Location permission handling** for GPS-based session tracking.

## How session metrics are collected

- **Distance** is calculated from location updates provided by Google Play services’ fused location provider.
- **Split pace** is calculated from location speed and displayed as time per 500 metres.
- **Stroke rate** is estimated from the device’s linear-acceleration sensor, or from the accelerometer if a linear-acceleration sensor is unavailable.
- **Elapsed time** is measured while a session is running.

Results depend on the Android device, sensor availability, GPS reception, and how the device is carried during a workout. Use the app outdoors with location services enabled for the best GPS results.

## Technology

- **Language:** Java
- **Platform:** Native Android
- **Build system:** Gradle with Kotlin DSL build files
- **Android Gradle Plugin:** 8.10.1
- **Compile SDK:** 35
- **Target SDK:** 35
- **Minimum SDK:** 29 (Android 10)
- **Java source and target compatibility:** Java 11
- **Backend services:** Firebase Authentication and Cloud Firestore
- **Location:** Google Play services Location

Other notable libraries include AndroidX AppCompat, Material Components, AndroidX Activity, and ConstraintLayout.

## Project structure

```text
JustRow/
├── app/
│   ├── build.gradle.kts              App configuration and dependencies
│   ├── google-services.json          Firebase configuration for the app
│   └── src/
│       ├── main/
│       │   ├── java/com/example/justrow/
│       │   │   ├── loginPage.java        Sign-in flow
│       │   │   ├── registrationPage.java Account creation and profile setup
│       │   │   ├── dashboard.java        Main navigation
│       │   │   ├── justRow.java          Standard rowing session
│       │   │   ├── customWorkout.java    Custom-distance setup
│       │   │   ├── customRow.java        Custom-distance session
│       │   │   ├── saveSession.java      Save a workout to Firestore
│       │   │   ├── previousSessions.java Workout history
│       │   │   └── detailedView.java     Workout details and sharing
│       │   ├── res/
│       │   │   ├── layout/               Screen layouts
│       │   │   ├── drawable/             App images and drawable resources
│       │   │   └── values/               Strings, colours, and themes
│       │   └── AndroidManifest.xml       Activities and app permissions
│       └── test/ and androidTest/        Unit and instrumentation test source sets
├── gradle/
│   └── libs.versions.toml            Central dependency and plugin versions
├── build.gradle.kts                  Root Gradle plugin configuration
├── settings.gradle.kts               Gradle project and repository setup
├── gradlew / gradlew.bat              Gradle wrapper scripts
└── gradle.properties                 Project-wide Gradle settings
```

## Requirements

To build and run JustRow, you need:

- Android Studio with Android SDK Platform 35 installed.
- JDK 17 or newer to run the Gradle build. The app’s Java source and bytecode compatibility are configured for Java 11.
- An Android device or emulator running Android 10 (API 29) or newer.
- A Firebase project configured for the app. Firebase Authentication and Cloud Firestore are used by the sign-in, registration, and workout-history features.
- A device with location services for GPS tracking. A motion sensor is needed for stroke-rate estimation.

## Firebase setup

The Android app uses Firebase Authentication and Cloud Firestore.

1. Create or select a Firebase project.
2. Add an Android app to the Firebase project with this application ID:

   ```text
   com.example.justrow
   ```

3. Download that Firebase app’s `google-services.json` and place it in the `app/` directory.
4. In Firebase Authentication, enable the **Email/Password** sign-in provider.
5. Create a Cloud Firestore database.
6. Configure Firestore Security Rules so each signed-in user can access only their own profile and workout data.
7. Sync the project with Gradle in Android Studio.

The app’s Firebase configuration file contains client-side project identifiers. It is not a substitute for backend security: enforce access control with Firebase Security Rules, and review Firebase API-key restrictions and project settings before distributing the app.

### Firestore data layout

The code stores user profiles and workouts in this structure:

```text
Users/
└── {userId}/
    ├── firstName
    ├── lastName
    ├── email
    ├── registration_date
    └── Workouts/
        └── {workoutId}/
            ├── title
            ├── saved_date
            ├── saved_time
            ├── distance
            ├── time
            └── averageSplit
```

Workout documents are associated with the authenticated user’s Firebase UID.

## Build and run

### Open in Android Studio

1. Clone the repository:

   ```bash
   git clone https://github.com/Lakitha-Senarathna/JustRow.git
   cd JustRow
   ```

2. Open the project directory in Android Studio.
3. Install Android SDK Platform 35 if prompted.
4. Confirm the Gradle JDK is set to JDK 17 or newer.
5. Ensure `app/google-services.json` is configured for your Firebase project.
6. Sync Gradle, then run the `app` configuration on an emulator or connected Android device.

### Build from the command line

On macOS or Linux:

```bash
./gradlew assembleDebug
```

On Windows:

```bat
gradlew.bat assembleDebug
```

The debug APK is normally created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

### Run tests

The project includes standard Gradle test source sets. Run the available local unit tests with:

```bash
./gradlew testDebugUnitTest
```

To run Android instrumentation tests on a connected device or emulator:

```bash
./gradlew connectedDebugAndroidTest
```

## Using the app

1. Launch JustRow. The login screen is the app’s launcher screen.
2. Register with an email address and password, or sign in with an existing account.
3. From the dashboard, choose **Just Row**, **Custom Workout**, or **Previous Sessions**.
4. Grant location permission when prompted to use GPS-based workout tracking.
5. For a custom workout, enter a target distance and start the session.
6. Stop or complete a session, save it with a title, and view it later in **Previous Sessions**.
7. Open a saved workout to view its details and share the workout summary.

## Permissions

The app requests:

- `INTERNET` for Firebase services.
- `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` for location-based workout tracking.

Location access is required for GPS distance and pace metrics. If permission is denied, those tracking features may not work as expected.

## Troubleshooting

- **Gradle sync or build fails:** Check that Android SDK Platform 35 is installed and Gradle is using JDK 17 or newer.
- **Firebase services do not connect:** Verify that `app/google-services.json` belongs to a Firebase Android app with application ID `com.example.justrow`, then sync Gradle again.
- **Registration or sign-in fails:** Confirm Email/Password is enabled in Firebase Authentication and check the Firebase project configuration.
- **Workout history cannot be loaded or saved:** Check the Firestore setup, internet connection, and Firestore Security Rules.
- **Distance or pace is unavailable or inaccurate:** Enable device location services and test outdoors with a clear view of the sky. GPS quality varies by device and environment.
- **Stroke rate remains at zero:** The device may not provide a suitable sensor, or the movement may not cross the app’s stroke-detection threshold.

## Contributing

This is an educational project. If you want to contribute, consider opening an issue to describe the proposed change before submitting a pull request. Please include the Android device or emulator details and reproduction steps when reporting a bug.

## License

No license is currently specified in this repository. Unless a license is added, reuse and redistribution may be restricted by default copyright rules.
