(ns re-natal-esp32control-app.views.ble-control.common
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def service-id "00ff")
(def characteristic-id "ff01")
(def joystick-img (js/require "./images/joystick.png"))

(defn ble-send [{:keys [lf lb rf rb]}]
  (let [current-device (subscribe [:get-current-device])
        data (map #(if (nil? %) 0 %) [lf lb rf rb])]
    (ble/write (:id @current-device) service-id characteristic-id data)))

(defn rate-x-y [p-x p-y view-x view-y view-w view-h]
  (let [harf-w (/ view-w 2)
        harf-h (/ view-h 2)
        rate-x (/ (- p-x view-x harf-w) harf-w)
        rate-y (/ (- p-y view-y harf-h) harf-h)]
    {:x rate-x :y rate-y}))

(defn joystick [id on-move on-release]
  (let [view-x (r/atom nil)
        view-y (r/atom nil)
        view-w 300
        view-h 300
        ref (str "joystick-" id)
        ref-obj (r/atom nil)
        update-view-x-y #(.measure @ref-obj (fn [fx fy w h px py]
                                              (reset! view-x px)
                                              (reset! view-y py)))
        action (fn [evt]
                 (-> (rate-x-y (.-pageX (.-nativeEvent evt))
                               (.-pageY (.-nativeEvent evt))
                               @view-x @view-y view-w view-h)
                     (on-move)))]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/image
         {:source joystick-img
          :style {:resize-mode "cover"
                  :background-color "#beb"
                  :align-self "center"}}
         [v.common/view
          {:ref ref
           :style {:width view-w
                   :height view-h}
           :on-layout #(update-view-x-y)
           :on-start-should-set-responder (fn [] true)
           :on-move-should-set-responder (fn [] true)
           :on-responder-grant #(action %)
           :on-responder-move #(action %)
           :on-responder-release #(on-release)}]])

      :component-did-mount
      #(reset! ref-obj (-> (.-refs %)
                           (aget ref)))})))
