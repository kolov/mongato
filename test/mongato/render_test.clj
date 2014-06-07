(ns mongato.render-test
  (:require [clojure.test :refer :all]
            [mongato.render :refer :all]
            [midje.sweet :refer :all])
  (:import [org.bson.types ObjectId])
  )

(fact "render-last4 renders ok"
      (count (-> (org.bson.types.ObjectId.) render-last4)) => 6
      )