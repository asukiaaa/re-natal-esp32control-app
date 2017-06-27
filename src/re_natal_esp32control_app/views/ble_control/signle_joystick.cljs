(ns re-natal-esp32control-app.views.ble-control.single-joystick
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.common :as v.ble-common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defn rate->byte [rate]
  (-> rate
      (min 1)
      (max 0)
      (* 255)))

(defn set-speed [{:keys [x y]}]
  (when (and x y)
    (let [f (rate->byte (- y))
          b (rate->byte y)
          l (rate->byte (- x))
          r (rate->byte x)
          lf (max 0 (- f l))
          rf (max 0 (- f r))
          lb (max 0 (- b l))
          rb (max 0 (- b r))
          speed {:lf (int lf) :rf (int rf) :lb (int lb) :rb (int rb)}]
      (dispatch [:set-speed speed]))))

(defn single-joystick-panel []
  (let [interval (r/atom nil)]
    (r/create-class
     {:reagent-render
      (fn []
        [v.ble-common/joystick :single-joystick #(set-speed %) #(set-speed {:x 0 :y 0})])
      :component-did-mount #(reset! interval (js/setInterval v.ble-common/send-speed 50))
      :component-will-unmount #(js/clearInterval @interval)})))
