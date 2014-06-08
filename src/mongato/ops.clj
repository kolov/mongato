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


(defn find-one-as-tmap [mongato ref]
  "Variation on find-one-as-map, returning result with metatinfo"
  (if-let [found (mc/find-one-as-map (:colname mongato) ref)] (mark-object found mongato)))

(defn find-tmaps
  "Variation on find-maps, returning result with metatinfo"
  ([mongato]
   (mark-sequence (mc/find-maps (:colname mongato)) mongato))
  ([mongato ref]
   (mark-sequence (mc/find-maps (:colname mongato) ref) mongato))
  ([mongato ref & fields]
   (mark-sequence (apply mc/find-maps (:colname mongato) ref fields) mongato))
  )


(defn save-and-return-tmap
  [mcoll doc]
  "Variation on save-and-return, returning result with metatinfo"
  (-> (mc/save-and-return (:colname mcoll) doc) (mark-object mcoll)))


(defn get-doc-by-field [table f-name f-val]
  "Get record by field f-name"
  (let [result (find-one-as-tmap table {f-name f-val})]
    result))
