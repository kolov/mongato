(ns mongato.demo
  (:require [mongato.core]
           )
  )


;(defdata "mongato.demo"
;         (entity identity
;                 :collection "identity"
;                 :view-hints
;                 [])
;         )
(defdata
  (collection
    user
    (:mongo-collection-name "user")
    (:finders ["id" "name"])
    (:view
     (:skip-fields :id)
     (:transform-fields [:uuid #(identity %)])
     )
    )
  (colelction identity)
  (colelction identity)
  )