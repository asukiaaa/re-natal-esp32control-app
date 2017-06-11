(ns re-natal-esp32control-app.views.top
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.views.common :as v.common]))

(def ReactNative (js/require "react-native"))
(def BleManager (js/require "react-native-ble-manager"))
(def logo-img (js/require "./images/esp32car.png"))

(def native-modules (.-NativeModules ReactNative))
(def ble-manager-module (.-BleManager native-modules))
(def native-event-emitter (.-NativeEventEmitter ReactNative))
(def ble-manager-emitter (new native-event-emitter ble-manager-module))
(def permissions-android (.-PermissionsAndroid ReactNative))
(def platform (.-Platform ReactNative))

(defn handle-discovered-peripheral [data]
  #_(v.common/alert "found device")
  (dispatch [:add-device (js->clj data :keywordize-keys true)]))

(defn ble-scan []
  (js/console.log :ble-scan)
  (-> (.scan BleManager (clj->js []) 5 true)
      (.then (fn []
               (v.common/alert "scan started")))))

(defn device-button [device]
  [v.common/touchable-highlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (dispatch [:set-page :ble-control])
                (dispatch [:set-current-device device]))}
   [v.common/text {:style {:color "#fff"}}
    [v.common/text (:name device) " " (:id device)]]])

(defn start-ble-manager []
  (-> (.start BleManager {:showAlert false :allowDuplication false})
      (.then (fn []
               (v.common/alert "module initialized"))))
  (.addListener ble-manager-emitter "BleManagerStopScan" #(v.common/alert "stopped scanning"))
  (.addListener ble-manager-emitter "BleManagerDiscoverPeripheral" handle-discovered-peripheral))

(defn ble-devices-box []
  (r/create-class
   {:reagent-render
    (fn []
      (let [devices (subscribe [:get-devices])]
        [v.common/view
         (if (empty? @devices)
           [v.common/text {:style {:margin 10}}
            "no device"]
           (for [device @devices]
             ^{:key (:id device)}
             [device-button device]))]))

    :component-did-mount
    (fn []
      (when (= (.-OS platform) "android")
        (if (< (.-Version platform) 23)
          (start-ble-manager)
          (let [permission-type (-> permissions-android
                                    (.-PERMISSIONS)
                                    (.-ACCESS_COARSE_LOCATION))]
            (-> (.check permissions-android permission-type)
                (.then (fn [result]
                         (v.common/alert (str "result " result))
                         (if result
                           (start-ble-manager)
                           (-> (.request permissions-android permission-type)
                               (.then (fn [result]
                                        (start-ble-manager)
                                        (v.common/alert (str "requested " result)))))))))))))}))

(defn top-page []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [v.common/view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [v.common/text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}}
        @greeting]
       [v.common/image {:source logo-img
                        :style  {:width 80 :height 80 :margin-bottom 30}}]
       [v.common/touchable-highlight
        {:style {:background-color "#999" :padding 10 :border-radius 5}
         :on-press (fn []
                     (dispatch [:set-devices []])
                     (ble-scan))}
        [v.common/text {:style {:color "white" :text-align "center" :font-weight "bold"}}
         "BLE scan"]]
       [ble-devices-box]])))
