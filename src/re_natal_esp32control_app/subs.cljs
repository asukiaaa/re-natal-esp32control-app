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
