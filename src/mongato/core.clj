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


;; Type metainfo
;;  "A Mongato collection consists of a
;;           - mongo collection name
;;           - render info"
(defrecord mongato [colname renderinfo])

(defn mark-type
  "attach mongato metainfo to an object"
  [object mong] (vary-meta map assoc ::mongato mong))


(defn replace-if-nil [val new-val] (if val val new-val))

(defn check-refs [refs minimum]
  (when (< (count refs) minimum)
    (throw (Exception. (str "Mimimum " minimum " elements expected after " (first refs) ", found " refs)))
    ))

(defn add-as-set [m refs]
  "Add to the current set value in a map or create a new set.
  (first refs) is the key
  (second refs) is the value: if set, merge both sets, otherwise add.
  Example: (add-as-set {:a #{:x}} [:a :b]) => {:a #{:b :x}}"
  (check-refs refs 2)
  (let [val (second refs)
        kw (first refs)
        m (update-in m [kw] replace-if-nil #{})]
    (if (set? val) (update-in m [kw] clojure.set/union val)
                   (update-in m [kw] conj val))))

(defn add-as-map [m refs]
  "Add to the current map value in a map or create a new map.
  (first refs) is the key
  (second refs) is the value: if map, merge both maps, otherwise this is a key in the second map, in which case
  (third refs) will be the value"
  (check-refs refs 2)
  (let [e2 (second refs)
        kw (first refs)
        m (update-in m [kw] replace-if-nil {})]
    (if (map? e2)
      [(drop 2 refs) (update-in m [kw] merge e2)]
      (do (check-refs refs 3)
          (let [e3 (nth refs 2)]
            (when (not (keyword? e2)) (throw (Exception. "Map or keyword expected here")))
            [(drop 3 refs) (update-in m [kw] assoc e2 e3)] )))))



(defn process-references [renderinfo references]
  (let [throw-error (fn [s] (throw (Exception. (str "One of :hide :by-type :by-name expected here, but [" s "] found instead."))))]
    (loop [ri renderinfo refs references]

      (if (not (= 0 (count refs)))
        (let [firstref (first refs)]
          (cond
            (not (keyword? firstref)) (throw-error firstref)
            ; hide expects one parameter keyword or a sequence
            (= :hide firstref) (recur (add-as-set ri refs) (drop 2 refs))
            (contains? #{:by-name :by-type} firstref) (let[[therest result] (add-as-map ri refs)] (recur result  therest))
            :default (throw-error firstref)
            ))
        ri))))

(defmacro defdata [name & references]
  (let [
         ; first optional parameter is collection name
         colname (when (string? (first references)) (first references))
         references (if colname (next references) references)
         colname (if colname colname (str name))
         ; optional renderinfo
         renderinfo (when (map? (first references)) (first references))
         references (if renderinfo (next references) references)
         renderinfo (if renderinfo renderinfo {})
         ; optional renderinfo pairs/triples
         renderinfo (process-references renderinfo references)
         ]
    `(def ~name (->mongato ~colname ~renderinfo))))

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

(defn render [object metainfo]
  (let [keys-to-hide (:hide metainfo #{})
        fn-by-name (:by-name metainfo {})
        fn-by-type (:by-type metainfo {})

        object (reduce (fn [m k] (dissoc m k))
                       object keys-to-hide)
        object (reduce (fn [m [k f]]
                         (let [curval (k m)] (if curval (assoc m k (f curval)) m)))
                       object fn-by-name)
        object (reduce (fn [m [k v]] (let [f (get fn-by-type (class v) identity)] (assoc m k (f v))))

                       {} object)

        ]

    object
    )
  )







