(ns mongato.core
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object ConvertToDBObject]]
            [monger.operators :refer :all]
            [clojure.java.io :as io]
            )
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]
           [java.math BigInteger]
           [java.security MessageDigest]
           [java.io PushbackReader])
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


;; general
; Copied from https://github.com/pmj/clojure-util/blob/master/util.clj

(defn- repeat-str
  "Concatenate num repetitions of rep-sc."
  [rep-sc num]
  (apply str (map (constantly rep-sc) (range 0 num))))

(defn bytes-to-hex
  "Returns a string of hex digits representing the given byte array. The string will always contain 2 digits for every byte, including any necessary leading zeroes."
  [byte-array]
  (let
      [hex (. (BigInteger. 1 byte-array) (toString 16))
       delta-len (- (* 2 (count byte-array)) (count hex))]
    (if (= 0 delta-len)
      hex
      (str (repeat-str "0" delta-len) hex))))

(defn sha1 [obj]
  (->> (.getBytes (.toString obj)) (.digest (MessageDigest/getInstance "SHA1")) bytes-to-hex))

;; Maps with type key

(def type-keyword :type)

(defn mark-type
  "Adds a :type entry"
  [map type] (assoc map #'type-keyword type))

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

(def ALL-TABLES
  "Contains the names of all table created with deftable"
  (atom []))

(defmacro deftable
  ([col-name table-name]
   `(do (def ~col-name ~table-name)
        (swap! ALL-TABLES #(conj % ~col-name))))
  ([name] (deftable name (name col-name))) )



;; for each entity xxx:
;; list-xxx - lists all
;; find-xxx
;;



