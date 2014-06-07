(ns mongato.ops-test
  (:require [clojure.test :refer :all]
            [mongato.core :refer :all]
            [mongato.ops :refer :all]
            [monger.collection :as mc]
            [midje.sweet :refer :all])
  (:import [org.bson.types ObjectId])
  )


(fact "config file read"
      (-> (conf "test-conf.clj") :key1) => 1)

(defdata people)

(def object1 {:_id (ObjectId.)})

(fact "Marking type"
      (mark-object object1 people) => object1
      (meta (mark-object object1 people)) => {:mongato.core/mongato people}
      )
(with-redefs
  [mc/find-one-as-map (constantly nil)]
  (fact "handles single not found"
        (mc/find-one-as-map people {}) => nil
        (find-one-as-tmap people {}) => nil
        ))

(with-redefs
  [mc/find-one-as-map (constantly object1)]
  (fact "Handles single found"
        (mc/find-one-as-map people {}) => object1
        (meta (mc/find-one-as-map people {})) => nil

        (find-one-as-tmap people {}) => object1
        (meta (find-one-as-tmap people {})) => {:mongato.core/mongato people}
        )
  )

(with-redefs
  [mc/find-maps (constantly [object1])]
  (fact "Handles multiple found"
        (mc/find-maps people {}) => [object1]
        (find-tmaps people {}) => [object1]

        )
  )