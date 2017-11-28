(ns re-natal-esp32control-app.events
  (:require
   [re-frame.core :refer [reg-event-db after]]
   [clojure.spec :as s]
   [re-natal-esp32control-app.db :as db :refer [app-db]]))

;; -- Interceptors ------------------------------------------------------------
;;
;; See https://github.com/Day8/re-frame/blob/master/docs/Interceptors.md
;;
(defn check-and-throw
  "Throw an exception if db doesn't have a valid spec."
  [spec db [event]]
  (when-not (s/valid? spec db)
    (let [explain-data (s/explain-data spec db)]
      (throw (ex-info (str "Spec check after " event " failed: " explain-data) explain-data)))))

(def validate-spec
  (if goog.DEBUG
    (after (partial check-and-throw ::db/app-db))
    []))

;; -- Handlers --------------------------------------------------------------

(reg-event-db
 :initialize-db
 validate-spec
 (fn [_ _]
   app-db))

(reg-event-db
 :set-greeting
 validate-spec
 (fn [db [_ value]]
   (assoc db :greeting value)))

(reg-event-db
 :add-device
 validate-spec
 (fn [db [_ device]]
   ; avoid duplicate saving
   ; may be bug of ble scanning
   (let [device-id (:id device)
         device-ids (map :id (:devices db))]
     (if (some #(= % device-id) device-ids)
       db
       (update db :devices #(conj % device))))))

(reg-event-db
 :set-devices
 validate-spec
 (fn [db [_ devices]]
   (assoc db :devices devices)))

(reg-event-db
 :set-current-device
 validate-spec
 (fn [db [_ device]]
   (assoc db :current-device device)))

(reg-event-db
 :set-page
 validate-spec
 (fn [db [_ page]]
   (assoc db :page page)))

(reg-event-db
 :set-speed
 (fn [db [_ speed]]
   (assoc db :speed speed)))

(reg-event-db
 :set-sent-speed
 (fn [db [_ speed]]
   (assoc db :sent-speed speed)))

(reg-event-db
 :set-mag-values
 (fn [db [_ mag-values]]
   (assoc db :mag-values mag-values)))

(reg-event-db
 :set-directoin-speed
 (fn [db [_ direction-speed]]
   (assoc db :direction-speed direction-speed)))

(reg-event-db
 :set-sent-direction-speed
 (fn [db [_ direction-speed]]
   (assoc db :sent-direction-speed direction-speed)))
