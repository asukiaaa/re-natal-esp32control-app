(ns re-natal-esp32control-app.views.ble-control.magnetometer
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.devices.magnetometer :as mag]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.common :as v.ble-common]))

(def box-w 50)
(def box-padding 5)
(def box-padded-w (- box-w (* 2 box-padding)))

(defn compass-panel []
  (let [mag-values (subscribe [:mag-values])]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view {:style {:width box-w :height box-w :background-color "#eee" :margin-bottom 10}}
         #_[v.common/text (:degree @mag-values)]
         [v.common/view {:style {:width box-padded-w :height box-padded-w
                                 :position :absolute :top box-padding :left box-padding
                                 :transform [{:rotate (str (or (:degree @mag-values) 0) "deg")}]}}
          [v.common/view {:style {:top 0
                                  :left (/ box-padded-w 4)
                                  :position "absolute"
                                  :width 0 :height 0
                                  :background-color "transparent"
                                  :border-style "solid"
                                  :border-left-width (/ box-padded-w 4)
                                  :border-right-width (/ box-padded-w 4)
                                  :border-bottom-width box-padded-w
                                  :borderLeftColor "transparent"
                                  :borderRightColor "transparent"
                                  :border-bottom-color "#f77"}}]]])
      :component-did-mount mag/start-monitoring
      :component-will-unmount mag/stop-monitoring})))

(defn- in-pi-roop [value]
  (cond-> value
    (> value 180) (- 360)
    (< value -180) (+ 360)))

(defn set-direction-speed [{:keys [x y]}]
  (let [mag-values (subscribe [:mag-values])
        mag-direction (:degree @mag-values)
        joystick-direction (-> (.atan2 js/Math x (- y))
                              (* 180)
                              (/ (.-PI js/Math)))
        direction (in-pi-roop (+ mag-direction joystick-direction))
        speed (-> (.sqrt js/Math (+ (* x x) (* y y)))
                  (min 1)
                  (* 255))]
    (dispatch [:set-directoin-speed {:degree direction :speed speed}])))

(defn mag-panel []
  (let [interval (r/atom nil)
        set-interval #(reset! interval (js/setInterval v.ble-common/send-direction 100))
        clear-interval #(js/clearInterval @interval)]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view {:style {:align-items "center"}}
         [compass-panel]
         [v.ble-common/joystick :compass-joystick #(set-direction-speed %) #(set-direction-speed {:x 0 :y 0})]])
      :component-did-mount set-interval
      :component-will-unmount clear-interval})))
