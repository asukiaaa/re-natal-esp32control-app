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

    :component-function
    config/app-root}))

#_(defn app-root []
  (let [greeting (subscribe [:get-greeting])]
    (fn []
      [view {:style {:flex-direction "column" :margin 40 :align-items "center"}}
       [text {:style {:font-size 30 :font-weight "100" :margin-bottom 20 :text-align "center"}} @greeting]
       [image {:source logo-img
               :style  {:width 80 :height 80 :margin-bottom 30}}]
       [touchable-highlight {:style {:background-color "#999" :padding 10 :border-radius 5}
                             :on-press #(alert "HELLO!")}
        [text {:style {:color "white" :text-align "center" :font-weight "bold"}} "press me"]]])))

(defn init []
  (config/init-once)
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component app-root)))

#_(defn init []
  (dispatch-sync [:initialize-db])
  (.registerComponent app-registry "reNatalEsp32ControlApp" #(r/reactify-component (r/reagent-render-component app-root))))
