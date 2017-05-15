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
