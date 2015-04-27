(ns mongato.util
  (:require
    [monger.operators :refer :all]
    )
  (:import
    (java.security MessageDigest)
    (java.math BigInteger)
    )
  )


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
