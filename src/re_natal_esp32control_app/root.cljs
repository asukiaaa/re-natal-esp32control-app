(ns re-natal-esp32control-app.root
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.devices.ble :as ble]
            [re-natal-esp32control-app.devices.magnetometer :as mag]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control :as v.ble-control]
            [re-natal-esp32control-app.views.top :as v.top]))

(defn app-root []
  (let [page (subscribe [:get-page])]
    (r/create-class
     {:component-will-mount
      (fn []
        (ble/init)
        (mag/init))
      :reagent-render
      (fn []
        (case @page
          :ble-control [v.ble-control/ble-control-page]
          [v.top/top-page]))})))
