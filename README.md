## Repository Rules
1. Branching: Always create a new branch for each task (git checkout -b branch-name).
2. Pull Requests: Submit pull requests for review by pushing your branch to this repository, never push directly to main.
   - Pull requests are subject to review to ensure that the main branch does not break.
3. Code Reviews: Ensure at least 2 reviewers approve before merging. Ping the discord server with @everyone.
4. Commit Messages: Use clear, concise commit messages in the format type: message.
5. No Large PRs: Keep pull requests small and focused on a single task.
6. No Sensitive Data: Never commit passwords or API keys to the repository.
7. Documentation: Update documentation when making changes to code or features.
8. **IMPORTANT - Rebasing**: Rebase your branch on main before submitting a pull request.

## Set up the environment

> **Warning**
> You need a Mac with macOS to write and run iOS-specific code on simulated or real devices.
> This is an Apple requirement.

To work with this template, you need the following:

* A machine running a recent version of macOS is required to run iOS code, if you are a windows user, please focus on developing in the common and android package.
* [Xcode MAC REQUIREMENT ONLY](https://apps.apple.com/us/app/xcode/id497799835)
* [Android Studio](https://developer.android.com/studio)
* [Kotlin Multiplatform Mobile plugin](https://plugins.jetbrains.com/plugin/14936-kotlin-multiplatform-mobile) - This can be installed via the plugins inside of the Android Studio IDE
* [CocoaPods dependency manager](https://kotlinlang.org/docs/native-cocoapods.html)


### Check your environment (MAC ONLY)

Before you start, use the [KDoctor](https://github.com/Kotlin/kdoctor) tool to ensure that your development environment is configured correctly:

1. Install KDoctor with [Homebrew](https://brew.sh/):

    ```text
    brew install kdoctor
    ```

2. Run KDoctor in your terminal:

    ```text
    kdoctor
    ```

   If everything is set up correctly, you'll see valid output:

   ```text
   Environment diagnose (to see all details, use -v option):
   [✓] Operation System
   [✓] Java
   [✓] Android Studio
   [✓] Xcode
   [✓] Cocoapods
   
   Conclusion:
     ✓ Your system is ready for Kotlin Multiplatform Mobile development!
   ```

Otherwise, KDoctor will highlight which parts of your setup still need to be configured and will suggest a way to fix them.

## Examine the project structure

Open the project in Android Studio and switch the view from **Android** to **Project** to see all the files and targets belonging to the project:

Your Compose Multiplatform project includes 3 modules:

### shared

This is a Kotlin module that contains the logic common for both Android and iOS applications, that is, the code you share between platforms.

This `shared` module is also where you’ll write your Compose Multiplatform code.
In `shared/src/commonMain/kotlin/App.kt`, you can find the shared root `@Composable` function for your app.

It uses Gradle as the build system. You can add dependencies and change settings in `shared/build.gradle.kts`.
The `shared` module builds into an Android library and an iOS framework.

### androidApp

This is a Kotlin module that builds into an Android application. It uses Gradle as the build system.
The `androidApp` module depends on and uses the `shared` module as a regular Android library.

### iosApp

This is an Xcode project that builds into an iOS application.
It depends on and uses the `shared` module as a CocoaPods dependency.

## Run your application

### On Android

To run your application on an Android emulator:

1. Ensure you have an Android virtual device available. Otherwise, [create one](https://developer.android.com/studio/run/managing-avds#createavd).
2. In the list of run configurations, select `androidApp`.
3. Choose your virtual device and click **Run**:

<details>
  <summary>Alternatively, use Gradle</summary>

To install an Android application on a real Android device or an emulator, run `./gradlew installDebug` in the terminal.

</details>

### On iOS (MAC ONLY)

#### Running on a simulator

To run your application on an iOS simulator in Android Studio, modify the `iosApp` run configuration:

1. In the list of run configurations, select **Edit Configurations**:
2. Navigate to **iOS Application** | **iosApp**.
3. In the **Execution target** list, select your target device. Click **OK**:
4. The `iosApp` run configuration is now available. Click **Run** next to your virtual device:
