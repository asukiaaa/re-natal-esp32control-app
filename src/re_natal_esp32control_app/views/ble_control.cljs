(ns re-natal-esp32control-app.views.ble-control
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.single-joystick :as v.single-joystick]
            [re-natal-esp32control-app.views.ble-control.tile-buttons :as v.tile-buttons]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(def control-modes
  [{:id :single-joystick
    :name "A Joystick"}
   {:id :tile-buttons
    :name "Tile Buttons"}])

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
         :tile-buttons [v.tile-buttons/tile-buttons-panel]
         [v.single-joystick/single-joystick-panel])])))

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
