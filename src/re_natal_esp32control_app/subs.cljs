(ns re-natal-esp32control-app.subs
  (:require [re-frame.core :refer [reg-sub]]))

(reg-sub
  :get-greeting
  (fn [db _]
    (:greeting db)))

(reg-sub
 :get-devices
 (fn [db _]
   (:devices db)))

(reg-sub
 :get-current-device
 (fn [db _]
   (:current-device db)))

(reg-sub
 :get-page
 (fn [db _]
   (:page db)))

(reg-sub
 :speed
 (fn [db _]
   (:speed db)))

(reg-sub
 :sent-speed
 (fn [db _]
   (:sent-speed db)))

(reg-sub
 :mag-values
 (fn [db _]
   (:mag-values db)))

(reg-sub
 :direction-speed
 (fn [db _]
   (:direction-speed db)))

(reg-sub
 :sent-direction-speed
 (fn [db _]
   (:sent-direction-speed db)))
