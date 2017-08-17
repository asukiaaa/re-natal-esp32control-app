(ns re-natal-esp32control-app.views.ble-control.toggle-bars
  (:require [clojure.string :as str]
            [reagent.core :as r]
            [re-natal-esp32control-app.views.common :as v.common]
            [re-natal-esp32control-app.views.ble-control.common :as v.ble-common]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]))

(defn- calc-rate [target base max-diff]
  (-> (/ (- target base) max-diff)
      (min 1)
      (max 0)))

(defn- bar-index [target base max-diff max-index]
  (-> (calc-rate target base max-diff)
      (* max-index)
      int
      (min (- max-index 1))))

(defn toggle-bars [id on-move on-release & [{:keys [width height] :or {width 300 height 300}}]]
  (let [bar-number 2
        view-x (r/atom nil)
        view-y (r/atom nil)
        ref (str "toggle-bars-" id)
        ref-obj (r/atom nil)
        circle-r 35
        default-indexes-rate {0 0.5, 1 0.5}
        indexes-rate (r/atom default-indexes-rate)
        update-view-x-y #(.measure @ref-obj (fn [fx fy w h px py]
                                              (reset! view-x px)
                                              (reset! view-y py)))
        on-release (fn []
                     (reset! indexes-rate default-indexes-rate)
                     (on-release default-indexes-rate))
        action (fn [evt]
                 (reset! indexes-rate default-indexes-rate)
                 (doall
                  (for [i (range (.-length (.-changedTouches (.-nativeEvent evt))))
                        :let [evt (aget (.-changedTouches (.-nativeEvent evt)) i)
                              index (bar-index (.-pageX evt) @view-x width bar-number)
                              rate (calc-rate (.-pageY evt) @view-y height)]]
                    (reset! indexes-rate (assoc @indexes-rate index rate))))
                 (on-move @indexes-rate))]
    (r/create-class
     {:reagent-render
      (fn []
        [v.common/view
         {:ref ref
          :style {:width width :height height
                  :align-self "center"
                  :flex-direction "row"}
          :on-layout #(update-view-x-y)
          :on-start-should-set-responder (fn [] true)
          :on-move-should-set-responder (fn [] true)
          :on-responder-grant #(action %)
          :on-responder-move #(action %)
          :on-responder-release #(on-release)}
         (doall
          (for [i (range bar-number)]
            ^{:key (str :bar- id i)}
            [v.common/view {:style {:width (- (/ width bar-number) 2) :height height
                                    :background-color "#ddd"
                                    :margin-left 1
                                    :margin-right 1}}
             [v.common/view {:style {:width (* 2 circle-r) :height (* 2 circle-r)
                                     :border-radius circle-r
                                     :background-color "#999"
                                     :position "absolute"
                                     :top (- (* height (get @indexes-rate i)) circle-r)
                                     :left (- (/ width bar-number 2) circle-r)}}]]))])
      :component-did-mount
      #(reset! ref-obj (-> (.-refs %)
                           (aget ref)))})))

(defn set-speed [indexes-rate]
  (let [rate->byte (fn [rate] (-> (- 1 rate)
                                  (* 255)
                                  (max 0)
                                  (min 255)
                                  int))
        speed {:l (rate->byte (get indexes-rate 0))
               :r (rate->byte (get indexes-rate 1))}]
    (dispatch [:set-speed speed])))

(defn toggle-bars-panel []
  (let [interval (r/atom nil)
        set-interval #(reset! interval (js/setInterval v.ble-common/send-speed 50))
        clear-interval #(js/clearInterval @interval)]
    (r/create-class
     {:reagent-render
      (fn []
        [toggle-bars :lr-bars #(set-speed %) #(set-speed %)])
      :component-did-mount set-interval
      :component-will-unmount clear-interval})))
