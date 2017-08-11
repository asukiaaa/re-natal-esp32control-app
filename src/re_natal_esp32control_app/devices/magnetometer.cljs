(ns re-natal-esp32control-app.devices.magnetometer
  (:require [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]))

(def ReactNative (js/require "react-native"))

(def device-event-emitter (.-DeviceEventEmitter ReactNative))
(def native-modules (.-NativeModules ReactNative))
(def sensor-manager (.-SensorManager native-modules))

(defn x-y->degree [data]
  (let [x (:x data)
        y (:y data)]
    (-> (.atan2 js/Math x y)
        (* 180)
        (/ (.-PI js/Math)))))

(defn on-scan-magnetometer [data]
  (let [data (js->clj data :keywordize-keys true)]
    (dispatch [:set-mag-values (assoc data :degree (x-y->degree data))])))

(defn init []
  (.addListener device-event-emitter "Magnetometer" on-scan-magnetometer))

(defn start-monitoring []
  (.startMagnetometer sensor-manager 500))

(defn stop-monitoring []
  (.stopMagnetometer sensor-manager))
