(ns re-natal-esp32control-app.views.ble-control
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def service-id "00ff")
(def characteristic-id "ff01")

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

(defn ble-send [device & [{:keys [lf lb rf rb]}]]
  (let [data (map #(if (nil? %) 0 %) [lf lb rf rb])]
    (ble/write (:id device) service-id characteristic-id data)))

(defn tile-buttons []
  (let [current-device (subscribe [:get-current-device])
        stop #(ble-send @current-device)
        control-button (fn [label signals]
                             [touch-button label #(ble-send @current-device signals) stop])]
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

(defn calc-rate-x-y [p-x p-y view-x view-y view-w view-h]
  (let [harf-w (/ view-w 2)
        harf-h (/ view-h 2)
        rate-x (/ (- p-x view-x harf-w) harf-w)
        rate-y (/ (- p-y view-y harf-h) harf-h)]
    {:x rate-x :y rate-y}))

(defn joystick [on-move on-release]
  (let [view-x (r/atom nil)
        view-y (r/atom nil)
        view-w 300
        view-h 300
        joystick-ref (r/atom nil)
        update-view-x-y #(.measure @joystick-ref (fn [fx fy w h px py]
                                                   (reset! view-x px)
                                                   (reset! view-y py)))
        action-after-calc (fn [evt]
                            (-> (calc-rate-x-y (.-pageX (.-nativeEvent evt))
                                               (.-pageY (.-nativeEvent evt))
                                               @view-x @view-y view-w view-h)
                                (on-move)))]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view
         {:ref "joystickArea"
          :style {:align-content "center" :align-self "center"
                  :width view-w
                  :height view-h
                  :background-color "#beb"}
          :on-layout (fn [this]
                       (update-view-x-y))
          :on-start-should-set-responder (fn [] true)
          :on-move-should-set-responder (fn [] true)
          :on-responder-grant #(action-after-calc %)
          :on-responder-move #(action-after-calc %)
          :on-responder-release (fn [] (on-release))}])

      :component-did-mount
      (fn [this]
        (reset! joystick-ref (-> (.-refs this)
                                 (.-joystickArea))))})))

(def control-modes
  [{:id :joystick
    :name "Joystick"}
   {:id :tile-buttons
    :name "Tile Buttons"}])

(defn all-range-action [{:keys [x y]}]
  (prn :x x)
  (prn :y y))

(defn control-area []
  (let [control-mode (r/atom (:id (first control-modes)))]
    (fn []
      [v.common/view
       [v.common/view {:style {:flex-direction "row"
                               :margin-bottom 40}}
        (doall
         (for [{:keys [id name]} control-modes
               :let [selected? (= @control-mode id)]]
           ^{:key (str :mode-select- id)}
           [v.common/touchable-highlight
            {:style {:background-color "#ccc"
                     :padding 10
                     :border-width 3
                     :border-color (if selected? "#666" "#ccc")
                     :margin 5
                     :border-radius 5}
             :on-press #(reset! control-mode id)}
            [v.common/text name]]))]
       (case @control-mode
         :tile-buttons [tile-buttons]
         [joystick #(all-range-action %) #(all-range-action {:x 0 :y 0})])])))

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
           [control-area]
           [v.common/view {:style {:align-content "center" :align-self "center"}}
            [v.common/text "connecting"]])])
      :component-will-mount (fn []
                              (ble/connect (:id @current-device)
                                           :on-success #(reset! connected? true)))
      :component-will-unmount #(ble/disconnect (:id @current-device))})))
