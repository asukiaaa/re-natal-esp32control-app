# Setup memo
Commands and setting that I did for setup.

```
re-natal init reNatalEsp32controlApp
cd re-natal-esp32-control-app
npm install --save react-native-ble-manager
npm install --save base64-js
re-natal use-component react-native-ble-manager
re-natal use-component base64-js
re-natal use-android-device real
re-natal use-figwheel
rect-native link
```

Added the followind info to `android/app/src/main/AndroidManifest.xml` to activate BLE on android.

```
<manifest
    ..
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.BLUETOOTH"/>
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN"/>
    <uses-feature android:name="android.hardware.bluetooth_le" android:required="true"/>

    <uses-sdk
        ..
        tools:overrideLibrary="it.innove" />
</manifest>
```
