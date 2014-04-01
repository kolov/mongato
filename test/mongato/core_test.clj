(ns mongato.core_test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [midje.sweet :refer :all]))

(fact "config file read"
      (-> (conf "core_test.clj") :key1) => 1)