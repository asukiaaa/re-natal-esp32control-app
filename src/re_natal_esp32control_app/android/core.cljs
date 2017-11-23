(ns re-natal-esp32control-app.android.core
  (:require [reagent.core :as r]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [re-natal-esp32control-app.events]
            [re-natal-esp32control-app.subs]
            [re-natal-esp32control-app.config :as config]))

(def ReactNative (js/require "react-native"))
(def app-registry (.-AppRegistry ReactNative))
(def back-handler (.-BackHandler ReactNative))

(defn on-back-pressed []
  (let [page (subscribe [:get-page])]
    (if (nil? @page)
      false
      (do
        (dispatch [:set-page nil])
        true))))

(defn app-root []
  (r/create-class
   {:component-will-mount
    #(.addEventListener back-handler "hardwareBackPress" on-back-pressed)

    :component-will-unmount
    #(.removeEventListener back-handler "hardwareBackPress" on-back-pressed)

    :reagent-render
    config/app-root}))

(defn init []
  (config/init-once)
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component app-root)))
