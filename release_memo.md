# Release memo
This is memo to remember what I did for releasing this app.

# Once per application

## Sign for apk
Set up apk with referencing this document: [react-native sidned-apk-android](https://facebook.github.io/react-native/docs/signed-apk-android.html).

Put keystore file to `android/app`.
Remember `STORE_PASSWORD` and `KEY_PASSWORD`.
They are usesd to configuring gradle files and releasing on play developper console.

```
cd android/app
keytool -genkey -v -keystore esp32-control-app-release-key.keystore -alias esp32-control-app-key-alias -keyalg RSA -keysize 2048 -validity 10000
```

Configure values with using pc environment file.

Create file and add following lines to `~/.gradle/gradle.properties`.
```
ESP32_CONTROL_APP_RELEASE_STORE_FILE=esp32-control-app-release-key.keystore
ESP32_CONTROL_APP_RELEASE_KEY_ALIAS=esp32-control-app-key-alias
ESP32_CONTROL_APP_RELEASE_STORE_PASSWORD=*****
ESP32_CONTROL_APP_RELEASE_KEY_PASSWORD=*****
```

Add or edit following lines on `android/app/build.gradle`.
```
...
android {
    ...
    defaultConfig { ... }
    signingConfigs {
        release {
            if (project.hasProperty('ESP32_CONTROL_APP_RELEASE_STORE_FILE')) {
                storeFile file(ESP32_CONTROL_APP_RELEASE_STORE_FILE)
                storePassword ESP32_CONTROL_APP_RELEASE_STORE_PASSWORD
                keyAlias ESP32_CONTROL_APP_RELEASE_KEY_ALIAS
                keyPassword ESP32_CONTROL_APP_RELEASE_KEY_PASSWORD
            }
        }
    }
    buildTypes {
        release {
            ...
            signingConfig signingConfigs.release
        }
    }
}
...
```

## Replace logo and image to your own
Remove default android and clojure logo.
If not, it will rejected because of copy right problem.

```
images/*
```

```
# 72x72px
android/app/src/main/res/mipmap-hdpi/*
# 48x48px
android/app/src/main/res/mipmap-mdpi/*
# 96x96px
android/app/src/main/res/mipmap-xhdpi/*
# 144x144px
android/app/src/main/res/mipmap-xxhdpi/*
```

## Permission change

Add following line to remove redundant permission.

For `android/app/build.gradle`.
```
android {
    ...
    buildTypes {
        debug {
            manifestPlaceholders = [excludeSystemAlertWindowPermission: "false"]
        }
        release {
            manifestPlaceholders = [excludeSystemAlertWindowPermission: "true"]
            ...
        }
    }
}
```

For `android/app/src/main/AndroidManifest.xml`.

```
<manifest
    ...
    xmlns:tools="http://schemas.android.com/tools">
    ...

    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" tools:remove="${excludeSystemAlertWindowPermission}"/>
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" tools:node="remove" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" tools:node="remove"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" tools:node="remove" />

</manifest>
```

## Assign different version and id to release and debug application
You can change version and id for debug and release app with adding the following setting to `android/app/build.gradle`.

```
android {
    ...
    buildTypes {
        debug {
            ...
            versionNameSuffix "-dev"
            applicationIdSuffix ".dev" // Cause error about Activity class when targetSdkversion is 23.
        }
    }
}
```

After that you can detect which is installed to your device by checking app info.

# Every time per release

## Update version
Google play developper console does not accept same version application.
If you are going to release second or later version, you need to change version on `android/app/build.gradle` like this.

```
android {
    ..
    defaultConfig {
    ..
        versionCode 2
        versionName "1.1"
    }
}
```

## Build

```
lein prod-build
cd android && ./gradlew assembleRelease
```

You use `android/app/build/outputs/apk/app-release.apk` for releasing.

## Test

Uninstall development application on your device because android cannot install same name application.

```
react-native run-android --variant=release
```

## Register apk on google play console for releasing.

## Develop again

```
re-natal use-figwheel
```

# References
- [re-natal production-build](https://github.com/drapanjanas/re-natal#production-build)
- [react-native sidned-apk-android](https://facebook.github.io/react-native/docs/signed-apk-android.html)
