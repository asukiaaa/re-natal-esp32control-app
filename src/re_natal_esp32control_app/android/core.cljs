(ns re-natal-esp32control-app.android.core
  (:require [clojure.string :as str]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]))

(def ReactNative (js/require "react-native"))
(def BleManager (js/require "react-native-ble-manager"))

(def app-registry (.-AppRegistry ReactNative))
(def text (reagent/adapt-react-class (.-Text ReactNative)))
(def view (reagent/adapt-react-class (.-View ReactNative)))
(def list-view (reagent/adapt-react-class (.-ListView ReactNative)))
(def image (reagent/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ReactNative)))
(def permissions-android (.-PermissionsAndroid ReactNative))
(def native-app-event-emitter (.-NativeAppEventEmitter ReactNative))

(def logo-img (js/require "./images/cljs.png"))

(defn handle-discovered-peripheral [data]
  (dispatch [:add-device (js->clj data :keywordize-keys true)]))

(defn ble-scan []
  (-> (.scan BleManager (clj->js []) 30 true)
      (.then (fn [results]
               (prn :finished-scan results)))))

(defn ble-devices-box []
  (reagent/create-class
   {:statics {:title "BLE devices"}
    :reagent-render (fn []
                      (let [devices (subscribe [:get-devices])]
                        [view
                         (if (empty? @devices)
                           [text {:style {:margin 10}}
                            "no device"]
                           (doall
                            (for [device @devices]
                              [text {:style {:margin 10}}
                               (:name device)])))]))
    :component-did-mount (fn [this]
                           (.start BleManager {:showAlert false})
                           (.addListener native-app-event-emitter "BleManagerDiscoverPeripheral" handle-discovered-peripheral)
                           (let [permission-type (-> (.-PERMISSIONS permissions-android)
                                                     (.-ACCESS_COARSE_LOCATION))]
                             (-> (.check permissions-android permission-type)
                                 (.then (fn [ok?]
                                          (if ok?
                                            nil #_(ble-scan)
                                            (js/alert (str "permission is bad"))))))))}))

(defn alert [title]
      (.alert (.-Alert ReactNative) title))

(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [image {:source logo-img
               :style  {:width 80 :height 80 :margin-bottom 30}}]
       [ble-devices-box]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press (fn []
                                         (alert "start scanning!")
                                         (dispatch [:set-devices []])
                                         (ble-scan))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "BLE scan"]]])))

(defn init []
      (dispatch-sync [:initialize-db])
      (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component app-root)))
