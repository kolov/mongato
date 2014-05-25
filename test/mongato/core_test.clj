(ns mongato.core_test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [midje.sweet :refer :all]))

(fact "config file read"
      (-> (conf "test-conf.clj") :key1) => 1)

(fact "rendering :hide"
      (render {:a 1 :b 2} {:hide #{:a :B}}) => {:b 2}
      (render {:a 1 :b 2} {:hide #{}}) => {:a 1 :b 2}
      (render {:a 1 :b 2} {}) => {:a 1 :b 2}
      )

(fact "rendering :by-name"
      (render {:a 1 :b 2} {:by-name {:a #(* 2 %)}}) => {:a 2 :b 2}
      )

(fact "rendering :by-type"
      (render {:a "a" :b 1} {:by-type {java.lang.String #(str "xx-" %)}}) => {:a "xx-a" :b 1}
      )

(fact "rendering all"
      (render {:_id 1 :a 11 :s "str"}
              {
                :hide    #{:_id}
                :by-name {:a #(* 2 %)}
                :by-type {java.lang.String #(str "xx-" %)}
                })

      => {:a 22 :s "xx-str"}
      )

(fact "adding ellement to a set value"
      (add-as-set {:a #{:x}} [:a :b]) => {:a #{:b :x}}
      (add-as-set {:a #{}} [:a :b]) => {:a #{:b}}
      (add-as-set {} [:a :b]) => {:a #{:b}}

      (add-as-set {} [:a]) => (throws Exception)
      )


(fact "addin ellement to a map value"
      (add-as-map {} [:a {:b :c} :d]) => ['(:d) {:a {:b :c}}]
      (add-as-map {} [:a :b :c :d]  ) => ['(:d) {:a {:b :c}}]
      (add-as-map {} [:a "b" :c]) => (throws Exception)
      (add-as-map {} [:a :b]) => (throws Exception)
      (add-as-map {} [:a]) => (throws Exception)

      )


(fact "creating mongato with name"
      (do (defdata x) (:colname x)) => "x"
      (do (defdata x "y") (:colname x)) => "y"
      (do (defdata x {}) (:renderinfo x)) => {}
      (do (defdata x {:a 1}) (:renderinfo x)) => {:a 1}
      )

(fact "Exceptions when references length wrong"
      (check-refs [:a 2] 3) => (throws Exception)
      (check-refs [:a 2] 2) => nil
      )

(fact "Processes erroneous references"
      (process-references {:x :y} ["x"]) => (throws Exception)
      (process-references {:x :y} [:hide #{:a} :unknown]) => (throws Exception)
      )

(fact "Processing hide reference"
      (process-references {} [:hide :a]) => {:hide #{:a}}
      (process-references {:x :y} [:hide :a]) => {:x :y :hide #{:a}}
      (process-references {:x :y} [:hide #{:a :b}]) => {:x :y :hide #{:a :b}}

      (process-references {:hide #{:c}} [:hide #{:a :b}]) => {:hide #{:a :b :c}}
      (process-references {:hide #{}} [:hide #{:a :b}]) => {:hide #{:a :b}}

      (process-references {} [:hide]) => (throws Exception)

      )

(defn fn1 [x] x)
(fact "Processing by-type"
      (process-references {} [:by-name :a fn1]) => {:by-name {:a fn1}}
      (process-references {:x :y} [:by-name :a fn1]) => {:x :y :by-name {:a fn1}}

      (process-references {} [:by-name :a fn1 :hide :c]) => {:by-name {:a fn1} :hide #{:c}}
      (process-references {} [:by-name :a fn1 :hide :c :by-type :d fn1]) => {:by-name {:a fn1} :hide #{:c} :by-type {:d fn1}}
      (process-references {} [:by-name :a fn1 :hide :c :by-type {:d fn1}]) => {:by-name {:a fn1} :hide #{:c} :by-type {:d fn1}}

      )

(defdata x1)
(defdata x2 "y2")
(defdata x3 "y3" :hide :a :by-name :b fn1 :by-name {:d fn1} :by-type :e fn1)

(fact "defdata executed ok"
      (:colname x1) => "x1"
      (:colname x2) => "y2"
      (:colname x3) => "y3"
      (:renderinfo x3)    => {:hide #{:a} :by-name {:b fn1 :d fn1} :by-type {:e fn1}}
      )



