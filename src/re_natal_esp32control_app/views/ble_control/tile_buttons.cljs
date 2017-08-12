(ns re-natal-esp32control-app.views.ble-control.tile-buttons
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.common :as v.ble-common]
            [re-frame.core :refer [dispatch]]))

(defn set-speed [speed]
  (dispatch [:set-speed (merge {:l 128 :r 128} speed)]))

(defn control-button [label speed]
  (let [set-and-send-speed (fn [speed]
                             (set-speed speed)
                             (v.ble-common/send-speed))]
    [v.common/view
     {:style {:background-color "#494" :width 100 :height 100 :margin 5 :border-radius 5}
      :justify-content "center"
      :align-items "center"
      :on-start-should-set-responder (fn [evt] true)
      :on-responder-grant #(set-and-send-speed speed)
      :on-responder-release #(set-and-send-speed {})}
     [v.common/text {:style {:color "#fff" :text-align "center" :height 100}}
      label]]))

(defn tile-buttons-panel []
  [v.common/view {:style {:align-content "center" :align-self "center"}}
   [v.common/view {:style {:flex-direction "row"}}
    [control-button "left foreward" {:r 255}]
    [control-button "forward"       {:l 255 :r 255}]
    [control-button "right-forward" {:l 255}]]
   [v.common/view {:style {:flex-direction "row"}}
    [control-button "turn left"     {:l 0 :r 255}]
    [v.common/view {:style {:width 100 :height 100 :margin 5}}]
    [control-button "trun right"    {:l 255 :r 0}]]
   [v.common/view {:style {:flex-direction "row"}}
    [control-button "back left"     {:r 0}]
    [control-button "back"          {:l 0 :r 0}]
    [control-button "back right"    {:l 0}]]])
