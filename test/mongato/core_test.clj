(ns mongato.core_test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [midje.sweet :refer :all])
  (:import (org.bson.types ObjectId)))


(defdata x)
(fact "Marked objects and sequences"
  (mark-object {} x) => {}
  (get-mongato (mark-object {} x)) => x
  (let [unmarked [{:a 1} {:a 2}]
        marked   (mark-sequence unmarked x)]
    (mark-sequence unmarked x) => unmarked
    (marked-sequence? unmarked) => falsey
    (marked-sequence? marked) => true
    (-> marked first get-mongato) => x
    )

  )

(fact "rendering  "
  (apply-renderinfo {:a 1 :b 2 :c 3} {:a #(str % %)
                                      :c (constantly nil)}) => {:b 2 :a "11"}
  )




(fact "adding ellement to a set value"
  (add-as-set {:a #{:x}} [:a :b]) => {:a #{:b :x}}
  (add-as-set {:a #{}} [:a :b]) => {:a #{:b}}
  (add-as-set {} [:a :b]) => {:a #{:b}}

  (add-as-set {} [:a]) => (throws Exception)
  )


(fact "addin ellement to a map value"
  (add-as-map {} [:a {:b :c} :d]) => ['(:d) {:a {:b :c}}]
  (add-as-map {} [:a :b :c :d]) => ['(:d) {:a {:b :c}}]
  (add-as-map {} [:a "b" :c]) => (throws Exception)
  (add-as-map {} [:a :b]) => (throws Exception)
  (add-as-map {} [:a]) => (throws Exception)

  )


(fact "creating mongato with name"
  (do (defdata x) (:colname x)) => "x"
  (do (defdata x "y") (:colname x)) => "y"
  (do (defdata x :renderinfo {:a 1}) (-> x :props :renderinfo)) => {:a 1}
  )

(fact "Exceptions when references length wrong"
  (check-refs [:a 2] 3) => (throws Exception)
  (check-refs [:a 2] 2) => nil
  )

(fact "Processes erroneous references"
  (process-references ["x"]) => (throws Error)
  (process-references [:p1 :p2 :p3]) => (throws Error)
  )



(defn fn1 [x] (str "-" x "-"))


(defdata x1)
(defdata x2 "y2")
(defdata x3 "y3" :renderinfo {:e fn1})


(fact "defdata executed ok"
  (:colname x1) => "x1"
  (:colname x2) => "y2"
  (:colname x3) => "y3"
  (:renderinfo (:props x3)) => {:e fn1}
  )

(defdata some-data :renderinfo {:c fn1})



(fact "printm"
  (let [
        unmarked-object {:c 1}
        marked-object   (mark-object unmarked-object some-data)
        unmarked-seq    [unmarked-object]
        marked-seq      (mark-sequence unmarked-seq some-data)
        ]
    (with-out-str (printm unmarked-object)) => "{:c 1}"
    (with-out-str (printm marked-object)) => "{:c -1-}"

    (with-out-str (printm unmarked-seq)) => "[{:c 1}]"
    (with-out-str (printm marked-seq)) => "({:c -1-})"

    ))


(fact "render-last4 renders ok"
  (count (-> (ObjectId.) last4)) => 6
  )
