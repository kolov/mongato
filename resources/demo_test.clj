(ns mongato.demo_test
  (:require [mongato.core]
            )
  )


(defn last4 [s] (str ".." (apply str (drop (- (count s) 4) s))))



(defdata
  USER "user"
  :mongo-collection "user"
  :finders ['find-by-id]
  :view {:skip-fields      [:_id]
         :transform-fields [:uuid #(-> % .toString last4)]
         }
  )


(defdata
  COOKIE "Cookie value"
  :finders ['find-by-id]
  :view {:skip-fields [:_id]
         :transform-fields
                      [
                        :uuid #(-> % .toString last4)
                        :value #(-> % last4)
                        ]
         }
  )