(ns re-natal-esp32control-app.devices.ble
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]))

(def ReactNative (js/require "react-native"))
(def BleManager (js/require "react-native-ble-manager"))

(def native-modules (.-NativeModules ReactNative))
(def ble-manager-module (.-BleManager native-modules))
(def native-event-emitter (.-NativeEventEmitter ReactNative))
(def ble-manager-emitter (new native-event-emitter ble-manager-module))
(def permissions-android (.-PermissionsAndroid ReactNative))
(def platform (.-Platform ReactNative))

(defn handle-discovered-peripheral [data]
  (dispatch [:add-device (js->clj data :keywordize-keys true)]))

(defn start-ble-manager []
  (-> (.start BleManager {:showAlert false :allowDuplication false})
      (.then (fn []
               (.enableBluetooth BleManager)
               #_(js/alert "module initialized"))))
  #_(.addListener ble-manager-emitter "BleManagerStopScan" #(js/alert "stopped scanning"))
  (.addListener ble-manager-emitter "BleManagerDiscoverPeripheral" handle-discovered-peripheral))

(defn init []
  (when (= (.-OS platform) "android")
    (if (< (.-Version platform) 23)
      (start-ble-manager)
      (let [access-permission (-> permissions-android
                                  (.-PERMISSIONS)
                                  (.-ACCESS_COARSE_LOCATION))]
        (-> (.check permissions-android access-permission)
            (.then (fn [result]
                     (if result
                       (start-ble-manager)
                       (-> (.request permissions-android access-permission)
                           (.then (fn [result]
                                    (start-ble-manager)
                                    #_(js/alert (str "requested " result)))))))))))))

(defn scan []
  (-> (.scan BleManager (clj->js []) 5 true)
      (.then (fn []
               #_(js/alert "scan started")))))

(defn connect [device-id & {:keys [on-success on-error]}]
  (-> (.connect BleManager device-id)
      (.then (fn []
               (when on-success (on-success))))
      (.catch (fn [error]
                (when on-error (on-error error))))))

(defn disconnect [device-id & {:keys [on-success on-error]}]
  (-> (.disconnect BleManager device-id)
      (.then (fn []
               (when on-success (on-success))))
      (.catch (fn [error]
                (when on-error (on-error error))))))

(defn write [device-id service-id chara-id data]
  (-> (.retrieveServices BleManager device-id)
      (.then (fn [peri-info]
               (.writeWithoutResponse BleManager device-id service-id chara-id
                                      (clj->js data))))
      (.catch (fn [error]
                (when (= error "Device is not connected")
                  (connect device-id
                           :on-success #(write device-id service-id chara-id data)))))))
