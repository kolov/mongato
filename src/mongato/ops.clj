(ns mongato.ops
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.java.io :as io]
            [monger.operators :refer :all]
            [mongato.core :refer :all]
            [jota.core :as log]
            )
  (:refer-clojure :exclude [remove])
  (:import
    (java.io PushbackReader)
    (java.util Map))
  )

;; Configure & Connect ::

(defn conf [location]
  (try (binding [*read-eval* false]
         (with-open [r (io/reader (io/resource location))]
           (read (PushbackReader. r))))
       (catch Exception _ (println "Could not read resource " location))))

(defn connect-with-params [db-config]
  "Connect to Mongodb from a map with parameters"
  (mg/connect! (merge {:host "localhost" :port 27017} db-config))
  (mg/authenticate (mg/get-db (:db db-config)) (:username db-config) (.toCharArray (:password db-config)))
  (mg/set-db! (mg/get-db (:db db-config))))

(defn connect-from-settings [location]
  "Connect to Mongodb with parameters from a file at location"
  (connect-with-params (conf location)))

;; Mongatos


(defn find-one-as-tmap
  "Variation on find-one-as-map, returning result with metatinfo"
  ([mongato ref]
   (mark-object (mc/find-one-as-map (get-colname mongato) ref) mongato))
  ([mongato ref fields]
   (mark-object (mc/find-one-as-map (get-colname mongato) ref fields) mongato))
  ([mongato ref fields keywordize]
   (mark-object (mc/find-one-as-map (get-colname mongato) ref fields keywordize) mongato)))

(defn find-tmaps
  "Variation on find-maps, returning result with metatinfo"
  ([mongato]
   (mark-sequence (mc/find-maps (get-colname mongato)) mongato))
  ([mongato ref]
   (mark-sequence (mc/find-maps (get-colname mongato) ref) mongato))
  ([mongato ref & fields]
   (mark-sequence (apply mc/find-maps (get-colname mongato) ref fields) mongato))
  )


(defn save-and-return-tmap
  [mcoll doc]
  "Variation on save-and-return, returning result with metatinfo"
  (-> (mc/save-and-return (:colname mcoll) doc) (mark-object mcoll)))


(defn get-doc-by-field
  "Get record by field f-name"
  ([table f-name f-val]
   (find-one-as-tmap table {f-name f-val}))
  ([table f-name f-val fields]
   (find-one-as-tmap table {f-name f-val} fields)))

(defn update-by-id [mongato id document]
  (mc/update-by-id (get-colname mongato) id document))

(defn remove [mongato ^Map conditions]
  (mc/remove (get-colname mongato) conditions))

(defn remove-by-id [mongato id]
  (mc/remove-by-id (get-colname mongato) id))

(defn find-tmap-by-id [mongato id]
  (mc/find-map-by-id (get-colname mongato) id))

;; shortcuts
(defn pprintc
  ([mongato] (pprintm (find-tmaps mongato)))
  ([mongato ref] (pprintm (find-tmaps mongato ref)))
  )
