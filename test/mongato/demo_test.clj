(ns mongato.demo-test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [midje.sweet :refer :all]))





; an example mongato here
(defdata x {:by-name #(* 2 %)})