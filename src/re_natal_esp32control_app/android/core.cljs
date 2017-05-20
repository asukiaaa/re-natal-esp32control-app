(ns re-natal-esp32control-app.android.core
  (:require [clojure.string :as str]
            [reagent.core :as reagent :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]))

(def ReactNative (js/require "react-native"))
(def BleManager (js/require "react-native-ble-manager"))
(def Base64 (js/require "base64-js"))

(def app-registry (.-AppRegistry ReactNative))
(def text (reagent/adapt-react-class (.-Text ReactNative)))
(def view (reagent/adapt-react-class (.-View ReactNative)))
(def list-view (reagent/adapt-react-class (.-ListView ReactNative)))
(def image (reagent/adapt-react-class (.-Image ReactNative)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ReactNative)))
(def permissions-android (.-PermissionsAndroid ReactNative))
(def native-app-event-emitter (.-NativeAppEventEmitter ReactNative))

(def logo-img (js/require "./images/cljs.png"))

(def service-id "00ff")
(def characteristic-id "ff01")

(defn alert [title]
  (.alert (.-Alert ReactNative) title))

(defn handle-discovered-peripheral [data]
  (prn :discovered data)
  (dispatch [:add-device (js->clj data :keywordize-keys true)]))

(defn ble-scan []
  (-> (.scan BleManager (clj->js []) 5 true)
      (.then (fn []
               (prn :finished-scan)))))

(defn device-button [device]
  [touchable-highlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (dispatch [:set-page :ble-control])
                (dispatch [:set-current-device device]))}
   [text {:style {:color "#fff"}}
    [text (:name device) " " (:id device)]]])

(defn ble-devices-box []
  (reagent/create-class
   {:reagent-render (fn []
                      (let [devices (subscribe [:get-devices])]
                        [view
                         (if (empty? @devices)
                           [text {:style {:margin 10}}
                            "no device"]
                           (for [device @devices]
                             [device-button device]))]))
    :component-will-mount (fn [this]
                           (.start BleManager {:showAlert false})
                           (.addListener native-app-event-emitter "BleManagerDiscoverPeripheral" handle-discovered-peripheral)
                           (let [permission-type (-> (.-PERMISSIONS permissions-android)
                                                     (.-ACCESS_COARSE_LOCATION))]
                             (-> (.check permissions-android permission-type)
                                 (.then (fn [ok?]
                                          (if ok?
                                            nil #_(ble-scan)
                                            (js/alert (str "permission is bad"))))))))}))

(defn control-button [label on-press on-release]
  [touchable-highlight {:style {:background-color "#494" :width 100 :height 100 :margin 5 :border-radius 5}
                        :justify-content "center"
                        :align-items "center"
                        :on-press on-press
                        :on-release on-release}
   [text {:style {:color "#fff" :text-align "center" :height 100}}
    label]])

(defn ble-send [device & {:keys [lf lb rf rb]}]
  (let [device (or device @(subscribe [:get-current-device]))]
    (.write BleManager (:id device) service-id characteristic-id
            (.fromByteArray Base64 (clj->js [lf lb rf rb])))))

(defn ble-control-page []
  (let [current-device (subscribe [:get-current-device])
        connected? (reagent/atom false)]
    (reagent/create-class
     {:reagent-render
      (fn []
        [view
         [touchable-highlight {:style {:background-color "#999" :padding 10 :width "100%"}
                               :on-press #(dispatch [:set-page nil])}
          [text {:style {:color "#fff"}}
           "<< back to top"]]
           [view {:style {:flex-direction "column" :align-items "flex-start" :margin 10}}
            [text {:style {:font-size 20 :font-weight "100" :width "100%"}}
             (:name @current-device)]
            [text (:id @current-device)]]
         (if @connected?
           [view {:style {:align-content "center" :align-self "center"}}
            [view {:style {:flex-direction "row"}}
             [control-button "left forward"  #(ble-send @current-device :rf 255)]
             [control-button "forward"       #(ble-send @current-device :lf 255 :rf 255)]
             [control-button "right-forward" #(ble-send @current-device :lf 255)]]
            [view {:style {:flex-direction "row"}}
             [control-button "turn left"     #(ble-send @current-device :lb 255 :rf 255)]
             [control-button "stop"          #(ble-send @current-device)]
             [control-button "trun right"    #(ble-send @current-device :lf 255 :rb 255)]]
            [view {:style {:flex-direction "row"}}
             [control-button "back left"     #(ble-send @current-device :rb 255)]
             [control-button "back"          #(ble-send @current-device :lb 255 :rb 255)]
             [control-button "back right"    #(ble-send @current-device :lb 255)]]]
           [view {:style {:align-content "center" :align-self "center"}}
            [text "connecting"]])])
      :component-will-mount #(-> (.connect BleManager (:id @current-device))
                                 (.then (fn [peri-info]
                                          (reset! connected? true)
                                          (prn :peri-info peri-info))))
      :component-will-unmount #(.disconnect BleManager (:id @current-device))})))

(defn top-page []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}}
        @greeting]
       [image {:source logo-img
               :style  {:width 80 :height 80 :margin-bottom 30}}]
       [touchable-highlight
        {:style {:background-color "#999" :padding 10 :border-radius 5}
         :on-press (fn []
                     (dispatch [:set-devices []])
                     (ble-scan))}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}}
         "BLE scan"]]
       [ble-devices-box]])))

(defn app-root []
  (.enableBluetooth BleManager)
  (let [page (subscribe [:get-page])]
    (fn []
      [view
       (case @page
         :ble-control [ble-control-page]
         [top-page])])))

(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(reagent/reactify-component app-root)))
