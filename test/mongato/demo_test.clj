(ns mongato.demo-test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [midje.sweet :refer :all]))





(fact "rendering :hide"
      (render {:a 1 :b 2} {:hide #{:a :B}}) => {:b 2}
      (render {:a 1 :b 2} {:hide #{}}) => {:a 1 :b 2}
      (render {:a 1 :b 2} {}) => {:a 1 :b 2}
      )

(fact "rendering :by-name"
      (render {:a 1 :b 2} {:by-name {:a #(* 2 %)}}) => {:a 2 :b 2}
      )

(fact "rendering all"
      (render {:a 1}

              {
                :hide #{:_id}
                :by-name
                      {
                        :a #(* 2 %)
                        }
                :by-type
                      {
                        org.bson.types.ObjectId identity
                        }
                })

      => {:a 2}
      )