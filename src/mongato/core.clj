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


(defrecord m-collection [mongo-col-name])


(defmacro defdata [col-name & references]
  (let [process-reference
        (fn [[kname & args]]
          `(~(symbol "clojure.core" (clojure.core/name kname))
            ~@(map #(list 'quote %) args)))
        mongo-col-name-string (when (string? (first references)) (first references))
        references (if mongo-col-name-string (next references) references)
        mongo-col-name (if mongo-col-name-string
                         mongo-col-name-string
                         (str col-name))
        metadata (when (map? (first references)) (first references))
        references (if metadata (next references) references)
        name (if metadata
               (vary-meta name merge metadata)
               name)
        gen-class-clause (first (filter #(= :gen-class (first %)) references))
        gen-class-call
        (when gen-class-clause
          (list* `gen-class :name (.replace (str name) \- \_) :impl-ns name :main true (next gen-class-clause)))
        references (remove #(= :gen-class (first %)) references)
        ;ns-effect (clojure.core/in-ns name)
        ]

    `(def ~col-name (->m-collection ~mongo-col-name))))

(defn find-one-as-tmap [mcoll ref]
  "Variation on find-one-as-map, returning a map with a :type entry collection name"
  (if-let [found (mc/find-one-as-map (:mongo-col-name mcoll) ref)] (mark-type found mcoll)))

(defn find-tmaps
  ([mcoll ref & fields]
   "Variation on find-maps, returning maps with a :type entry"
   (map #(mark-type % mcoll) (apply mc/find-maps (:mongo-col-name mcoll) ref fields)))
  )

(defn save-and-return-tmap
  [mcoll doc]
  "Variation on save-and-return, returning map with a :type entry"
  (-> (mc/save-and-return (:mongo-col-name mcoll) doc) (mark-type mcoll)))


(defn get-doc-by-field [table f-name f-val]
  "Get record by field f-name"
  (let [result (find-one-as-tmap table {f-name f-val})]
    result))







