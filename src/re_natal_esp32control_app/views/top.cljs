(ns re-natal-esp32control-app.views.top
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.views.common :as v.common]))

(def logo-img (js/require "./images/esp32car.png"))

(defn device-button [device]
  [v.common/touchable-highlight
   {:style {:background-color "#4a4" :border-radius 5 :padding 10 :margin 5}
    :on-press (fn []
                (dispatch [:set-page :ble-control])
                (dispatch [:set-current-device device]))}
   [v.common/text {:style {:color "#fff"}}
    [v.common/text (:name device) " " (:id device)]]])

(defn ble-devices-box []
  (let [devices (subscribe [:get-devices])]
    (fn []
      [v.common/view
       (if (empty? @devices)
         [v.common/text {:style {:margin 10}}
          "no device"]
         [v.common/flat-list
          {:data (clj->js @devices)
           :key-extractor (fn [item index]
                            (:id (js->clj item :keywordize-keys true)))
           :render-item #(let [device (:item (js->clj % :keywordize-keys true))]
                           (r/as-element [device-button device]))}])])))

(defn top-page []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [v.common/scroll-view
       [v.common/view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
        [v.common/text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}}
         @greeting]
        [v.common/image {:source logo-img
                         :style  {:width 80 :height 80 :margin-bottom 30}}]
        [v.common/touchable-highlight
         {:style {:background-color "#999" :padding 10 :border-radius 5}
          :on-press (fn []
                      (dispatch [:set-devices []])
                      (ble/scan))}
         [v.common/text {:style {:color "white" :text-align "center" :font-weight "bold"}}
          "BLE scan"]]
        [ble-devices-box]]])))
