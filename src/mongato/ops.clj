(ns mongato.ops
  (:import (com.mongodb MongoClient)
           (java.math BigInteger))
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [clojure.java.io :as io]
            [monger.conversion :refer [from-db-object ConvertToDBObject]]
            [monger.operators :refer :all]
            [mongato.core :refer :all]
            )
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]
           (java.io PushbackReader)
           (com.mongodb MongoClient)
           )
  )

;; Configure & Connect ::

(defn conf [location]
  (try (binding [*read-eval* false]
         (with-open [r (io/reader (io/resource location))]
           (read (PushbackReader. r))))
       (catch Exception e (println "Could not read resource " location))))

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


(defn get-doc-by-field [table f-name f-val]
  "Get record by field f-name"
  (log/debug "get-doc with " f-name "=" f-val)
  (let [result (find-one-as-tmap table {f-name f-val})]
    (log/debug "result " result)
    result))

(defn update-by-id [mongato id document]
  (mc/update-by-id (get-colname mongato) id document))

(defn remove-by-id [mongato id]
  (mc/remove-by-id (get-colname mongato) id))

(defn find-tmap-by-id [mongato id]
  (mc/find-map-by-id (get-colname mongato) id))

;; shortcuts
(defn mlist
  ([mongato] (render (find-tmaps mongato)))
  ([mongato ref] (render (find-tmaps mongato ref)))
  )
