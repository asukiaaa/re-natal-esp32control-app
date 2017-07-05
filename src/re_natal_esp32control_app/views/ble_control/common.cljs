(ns re-natal-esp32control-app.views.ble-control.common
  (:require [reagent.core :as r]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def moment (js/require "moment"))

(def service-id "00ff")
(def characteristic-id "ff01")

(defn ble-send [{:keys [lf lb rf rb]}]
  (let [current-device (subscribe [:get-current-device])
        data (map #(if (nil? %) 0 %) [lf lb rf rb])]
    (ble/write (:id @current-device) service-id characteristic-id data)))

(defn- same-speed? [speed1 speed2]
  (and (= (:lf speed1) (:lf speed2))
       (= (:lb speed1) (:lb speed2))
       (= (:rf speed1) (:rf speed2))
       (= (:rb speed1) (:rb speed2))))

(defn- zero-speed? [speed]
  (empty?
   (for [key [:lf :lb :rf :rb]
         :when (not (zero? (or (key speed) 0)))]
     key)))

(defn send-speed []
  (let [speed (subscribe [:speed])
        sent-speed (subscribe [:sent-speed])
        now-500ms (.subtract (moment) 500 "ms")]
    (when (or (not (same-speed? @speed @sent-speed))
              (and (not (zero-speed? @speed))
                   (.isAfter now-500ms (moment (:sent_at @sent-speed)))))
      (ble-send @speed)
      (dispatch [:set-sent-speed (assoc @speed :sent_at (js/Date.))]))))

(defn- rate-x-y [p-x p-y view-x view-y view-w view-h]
  (let [harf-w (/ view-w 2)
        harf-h (/ view-h 2)
        rate-x (/ (- p-x view-x harf-w) harf-w)
        rate-y (/ (- p-y view-y harf-h) harf-h)]
    {:x rate-x :y rate-y}))

(defn joystick [id on-move on-release & [{:keys [width height] :or {width 300 height 300}}]]
  (let [view-x (r/atom nil)
        view-y (r/atom nil)
        view-w width
        view-h height
        center-r 75
        target-r 50
        posi-x-base (/ view-w 2)
        posi-y-base (/ view-h 2)
        value-x (r/atom 0)
        value-y (r/atom 0)
        ref (str "joystick-" id)
        ref-obj (r/atom nil)
        update-view-x-y #(.measure @ref-obj (fn [fx fy w h px py]
                                              (reset! view-x px)
                                              (reset! view-y py)))
        on-move (fn [{:keys [x y] :as xy}]
                  (reset! value-x x)
                  (reset! value-y y)
                  (on-move xy))
        on-release (fn []
                     (reset! value-x 0)
                     (reset! value-y 0)
                     (on-release))
        action (fn [evt]
                 (-> (rate-x-y (.-pageX (.-nativeEvent evt))
                               (.-pageY (.-nativeEvent evt))
                               @view-x @view-y view-w view-h)
                     (on-move)))]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view
         {:ref ref
          :style {:background-color "#beb"
                  :align-self "center"
                  :width view-w
                  :height view-h}
          :on-layout #(update-view-x-y)
          :on-start-should-set-responder (fn [] true)
          :on-move-should-set-responder (fn [] true)
          :on-responder-grant #(action %)
          :on-responder-move #(action %)
          :on-responder-release #(on-release)}
         [v.common/view {:style {:background-color "#fff"
                                 :width (* center-r 2)
                                 :height (* center-r 2)
                                 :border-radius center-r
                                 :position "absolute"
                                 :top (- (/ view-h 2) center-r)
                                 :left (- (/ view-w 2) center-r)}}]
         [v.common/view {:style {:background-color "#777"
                                 :width (* target-r 2)
                                 :height (* target-r 2)
                                 :border-radius target-r
                                 :position "absolute"
                                 :top (- (+ posi-y-base (* posi-y-base @value-y)) target-r)
                                 :left (- (+ posi-x-base (* posi-x-base @value-x)) target-r)}}]])

      :component-did-mount
      #(reset! ref-obj (-> (.-refs %)
                           (aget ref)))})))
