(ns re-natal-esp32control-app.views.ble-control
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def service-id "00ff")
(def characteristic-id "ff01")

(defn control-button [label on-press on-release]
  [v.common/touchable-highlight
   {:style {:background-color "#494" :width 100 :height 100 :margin 5 :border-radius 5}
    :justify-content "center"
    :align-items "center"
    :on-press on-press
    :on-release on-release}
   [v.common/text {:style {:color "#fff" :text-align "center" :height 100}}
    label]])

(defn ble-send [device & {:keys [lf lb rf rb]}]
  (let [data (map #(if (nil? %) 0 %) [lf lb rf rb])]
    (ble/write (:id device) service-id characteristic-id data)))

(defn ble-control-page []
  (let [current-device (subscribe [:get-current-device])
        connected? (r/atom false)]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view
         [v.common/touchable-highlight {:style {:background-color "#999" :padding 10 :width "100%"}
                               :on-press #(dispatch [:set-page nil])}
          [v.common/text {:style {:color "#fff"}}
           "<< back to top"]]
           [v.common/view {:style {:flex-direction "column" :align-items "flex-start" :margin 10}}
            [v.common/text {:style {:font-size 20 :font-weight "100" :width "100%"}}
             (:name @current-device)]
            [v.common/text (:id @current-device)]]
         (if @connected?
           [v.common/view {:style {:align-content "center" :align-self "center"}}
            [v.common/view {:style {:flex-direction "row"}}
             [control-button "left forward"  #(ble-send @current-device :rf 255)]
             [control-button "forward"       #(ble-send @current-device :lf 255 :rf 255)]
             [control-button "right-forward" #(ble-send @current-device :lf 255)]]
            [v.common/view {:style {:flex-direction "row"}}
             [control-button "turn left"     #(ble-send @current-device :lb 255 :rf 255)]
             [control-button "stop"          #(ble-send @current-device)]
             [control-button "trun right"    #(ble-send @current-device :lf 255 :rb 255)]]
            [v.common/view {:style {:flex-direction "row"}}
             [control-button "back left"     #(ble-send @current-device :rb 255)]
             [control-button "back"          #(ble-send @current-device :lb 255 :rb 255)]
             [control-button "back right"    #(ble-send @current-device :lb 255)]]]
           [v.common/view {:style {:align-content "center" :align-self "center"}}
            [v.common/text "connecting"]])])
      :component-will-mount (fn []
                              (ble/connect (:id @current-device)
                                           :on-success #(reset! connected? true)))
      :component-will-unmount #(ble/disconnect (:id @current-device))})))
