(ns re-natal-esp32control-app.android.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control :as v.ble-control]
            [re-natal-esp32control-app.views.top :as v.top]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))

(defn app-root []
  (let [page (subscribe [:get-page])]
    (fn []
      (case @page
        :ble-control [v.ble-control/ble-control-page]
        [v.top/top-page]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (ble/init)
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component app-root)))
