# Release memo
This is memo to remember what I did for releasing this app.

# Replace logo and image to your own
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

# APK setup
Set up apk with referencing this document: [react-native sidned-apk-android](https://facebook.github.io/react-native/docs/signed-apk-android.html).

# Update version
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

# Build

```
lein prod-build
cd android && ./gradlew assembleRelease
```

You use `android/app/build/outputs/apk/app-release.apk` for releasing.

# Test

Uninstall development application on your device because android cannot install same name application.

```
react-native run-android --variant=release
```

# Register apk on google play console for releasing.

# Develop again

```
re-natal use-figwheel
```

References
- [re-natal production-build](https://github.com/drapanjanas/re-natal#production-build)
- [react-native sidned-apk-android](https://facebook.github.io/react-native/docs/signed-apk-android.html)
