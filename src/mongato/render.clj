(ns mongato.render
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

; Useful render functions

(defn render-last4 [id]
  "Renders only the end of an id, e.g.: ..NNNN"
  (let[s (str id)]
  (str ".." (apply str (drop (- (count s) 4) s)))
  ))
