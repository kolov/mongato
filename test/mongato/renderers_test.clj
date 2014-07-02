(ns mongato.renderers-test
  (:require [clojure.test :refer :all]
            [mongato.renderers :refer :all]
            [midje.sweet :refer :all])
  (:import [org.bson.types ObjectId])
  )

(fact "render-last4 renders ok"
      (count (-> (org.bson.types.ObjectId.) last4)) => 6
      )