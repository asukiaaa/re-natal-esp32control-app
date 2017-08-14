(ns re-natal-esp32control-app.views.ble-control.magnetometer
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.devices.magnetometer :as mag]
            [re-natal-esp32control-app.views.common :as v.common]))

(def box-w 150)
(def ball-r 20)

(defn compass-panel []
  (let [mag-values (subscribe [:mag-values])]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view {:style {:width box-w :height box-w :background-color "#eee" :margin-bottom 10}}
         [v.common/view {:style {:width box-w :height box-w
                                 :position :absolute :top 0 :left 0
                                 :transform [{:rotate (str (or (+ (:degree @mag-values) 90) 0) "deg")}]}}
          [v.common/view {:style {:top 0
                                  :left (/ box-w 4)
                                  :position "absolute"
                                  :width 0 :height 0
                                  :background-color "transparent"
                                  :border-style "solid"
                                  :border-left-width (/ box-w 4)
                                  :border-right-width (/ box-w 4)
                                  :border-bottom-width box-w
                                  :borderLeftColor "transparent"
                                  :borderRightColor "transparent"
                                  :border-bottom-color "#f77"}}]]
         [v.common/text (:x @mag-values)]
         [v.common/text (:y @mag-values)]
         [v.common/text (:degree @mag-values)]])
      :component-did-mount mag/start-monitoring
      :component-will-unmount mag/stop-monitoring})))

(defn mag-panel []
  [v.common/view {:style {:align-items "center"}}
   [compass-panel]])
