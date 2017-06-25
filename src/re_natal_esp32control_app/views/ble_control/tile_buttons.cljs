(ns re-natal-esp32control-app.views.ble-control.tile-buttons
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.common :as v.ble-common]))

(defn touch-button [label on-touch on-release]
  [v.common/view
   {:style {:background-color "#494" :width 100 :height 100 :margin 5 :border-radius 5}
    :justify-content "center"
    :align-items "center"
    :on-start-should-set-responder (fn [evt] true)
    :on-responder-grant #(on-touch)
    :on-responder-release #(on-release)}
   [v.common/text {:style {:color "#fff" :text-align "center" :height 100}}
    label]])

(defn tile-buttons-panel []
  (let [stop #(v.ble-common/ble-send {})
        control-button (fn [label signals]
                             [touch-button label #(v.ble-common/ble-send signals) stop])]
    [v.common/view {:style {:align-content "center" :align-self "center"}}
     [v.common/view {:style {:flex-direction "row"}}
      [control-button "left foreward" {:rf 255}]
      [control-button "forward"       {:lf 255 :rf 255}]
      [control-button "right-forward" {:lf 255}]]
     [v.common/view {:style {:flex-direction "row"}}
      [control-button "turn left"     {:lb 255 :rf 255}]
      [v.common/view {:style {:width 100 :height 100 :margin 5}}]
      [control-button "trun right"    {:lf 255 :rb 255}]]
     [v.common/view {:style {:flex-direction "row"}}
      [control-button "back left"     {:rb 255}]
      [control-button "back"          {:lb 255 :rb 255}]
      [control-button "back right"    {:lb 255}]]]))
