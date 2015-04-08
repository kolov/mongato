(ns mongato.renderers)

; Useful render functions

(defn last4 [id]
  "Renders only the end of an id, e.g.: ..NNNN"
  (let [s (str id)]
    (str ".." (apply str (drop (- (count s) 4) s)))
    ))
