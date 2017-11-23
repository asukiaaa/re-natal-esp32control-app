(ns re-natal-esp32control-app.android.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.config :as config]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))

(defn app-root []
  (config/app-root))

(defn init []
  (config/init-once)
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component app-root)))
