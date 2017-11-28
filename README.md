# re-natal-esp32control-app

A Clojure library designed to control car over BLE connection.

[![image](doc/google_play.png)](https://play.google.com/store/apps/details?id=com.renatalesp32controlapp)

If you want to make simillar application, [setup memo](/setup_memo.md) may help you.

If you want reference how to release this app, [release memo](/release_memo.md) may help you.

# Usage
Install dependencies.
(Using `yarn` causes production build error at `app:bundleReleaseJsAndAssets`.)
```
npm install
```

If you want to develop with using figwheel.
```
adb reverse tcp:8081 tcp:8081
adb reverse tcp:3449 tcp:3449
lein figwheel android
```

For linux.
```
react-native start
```

Connect your android device to pc and execute the following command to run application on android.
```
react-native run-android
```

# License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

# References
- [react-native-ble-manager](https://github.com/innoveit/react-native-ble-manager)
- [android manifest](https://developer.android.com/studio/build/manifest-merge.html)
- [android BLE](https://developer.android.com/guide/topics/connectivity/bluetooth-le.html)
- [Gesture Responder System](https://facebook.github.io/react-native/docs/gesture-responder-system.html)
