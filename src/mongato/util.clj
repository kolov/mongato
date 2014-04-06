(ns mongato.util
  (:import (com.mongodb MongoClient)
           (java.math BigInteger))
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.conversion :refer [from-db-object ConvertToDBObject]]
            [monger.operators :refer :all]
            [clojure.java.io :as io]
            [clojure.core.typed :refer :all]
            )
  (:import [com.mongodb MongoOptions ServerAddress]
           [org.bson.types ObjectId]
           (java.io PushbackReader)
           (java.security MessageDigest)
           (com.mongodb MongoClient)
           )
  )




;; missing utils
; Copied from https://github.com/pmj/clojure-util/blob/master/util.clj

(defn- repeat-str
  "Concatenate num repetitions of rep-sc."
  [rep-sc num]
  (apply str (map (constantly rep-sc) (range 0 num))))

(defn bytes-to-hex
  "Returns a string of hex digits representing the given byte array.
  The string will always contain 2 digits for every byte, including any necessary leading zeroes."
  [byte-array]
  (let
      [hex (. (BigInteger. 1 byte-array) (toString 16))
       delta-len (- (* 2 (count byte-array)) (count hex))]
    (if (= 0 delta-len)
      hex
      (str (repeat-str "0" delta-len) hex))))

(defn sha1 [obj]
  (->> (.getBytes (.toString obj))
       (.digest (MessageDigest/getInstance "SHA1"))
       bytes-to-hex))

(defn uuid [] (sha1 (java.util.UUID/randomUUID)))
