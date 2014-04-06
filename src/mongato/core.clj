(ns mongato.core
  (:import (com.mongodb MongoClient)
           (java.math BigInteger))
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object ConvertToDBObject]]
            [monger.operators :refer :all]
            [clojure.java.io :as io]
            [mongato.util :refer :all]
            [clojure.core.typed :refer :all]
            )
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]
           (java.io PushbackReader)
           (java.security MessageDigest)
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



;; Maps with type key

(def TYPE ::type)

(defn mark-type
  "Adds a :type entry"
  [map type] (vary-meta map TYPE type))

(defn find-one-as-tmap [coll ref]
  "Variation on find-one-as-map, returning a map with a :type entry collection name"
  (if-let [found (mc/find-one-as-map coll ref)] (mark-type found coll)))

(defn find-tmaps
  ([coll ref & fields]
   "Variation on find-maps, returning maps with a :type entry"
   (map #(mark-type % coll) (apply mc/find-maps coll ref fields)))
  )


;-- db access
(defn get-doc-by-field [table f-name f-val]
  "Get record by field f-name"
  (let [result (find-one-as-tmap table {f-name f-val})]
    result))

(def ^{:private true} collections
  "Contains the names of all table created with deftable"
  (atom {}))

(defn all-collections [] (keys @collections))
(defn all-mongo-collections [] (vals @collections))


(defmacro defcollection
  ([col-name mongo-col-name]
   `(do (swap! collections #(assoc % '~col-name ~mongo-col-name))
        (def ~col-name ~mongo-col-name)
        ))
  ([col-name] `(defcollection ~col-name ~(str col-name))  ))

(defmacro deffinder [col-name field]
  `(defn ~(symbol (str "find-" col-name "-by-" field)) [v#]
     find-one-as-tmap { ~(keyword field) v#}))

(defmacro deflist [col-name]
  `(defn ~(symbol (str "list-" col-name)) []  (find-tmaps ~col-name {})))



