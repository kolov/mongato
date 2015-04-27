(ns mongato.core
  (:require
    [monger.operators :refer :all]
    [mongato.util :refer :all]
    [clojure.pprint :refer [pprint]]
    [clojure.set]
    ))


;; Type metainfo
;;  A Mongato consists of a
;;           - mongo collection name
;;           - properties
(defrecord mongato [colname props])
(defn get-colname [m] (if-let [colname (:colname m)] colname (throw (Exception. (str m " has no :colname. Is it a mongato?")))))

(defn all-mongatos [ns]
  (->> (seq (ns-publics ns))
    (filter (fn [[k v]] (= "mongato.core.mongato" (.getName (class (var-get v))))))
    (map (fn [[k v]] [k (var-get v)]))
    ))

(defn all-collection-names [ns]
  (map (fn [[_ v]] (get-colname v)) (all-mongatos ns)))


(defn mark-object
  "attach mongato metainfo to an object"
  [object mong] (if object (vary-meta object assoc ::mongato mong)))

(defn mark-sequence
  "attach mongato metainfo to a seq of objects"
  [mcoll mong]
  (let [marked (map #(mark-object % mong) mcoll)]
    (vary-meta marked assoc ::mongato-collection true)))

(defn marked-sequence? [s]
  "true if the sequence was marked as containing mangatos"
  (-> s meta ::mongato-collection))

(defn get-mongato [x] (-> x meta ::mongato))

(defn replace-if-nil [val new-val] (if val val new-val))

(defn check-refs [refs minimum]
  (when (< (count refs) minimum)
    (throw (Exception. (str "Mimimum " minimum " elements expected after " (first refs) ", found " refs)))
    ))

(defn add-as-set [m refs]
  "Add to the current set value in a map or create a new set.
  (first refs) is the key
  (second refs) is the value: if set, merge both sets, otherwise add.
  Example: (add-as-set {:a #{:x}} [:a :b]) => {:a #{:b :x}}"
  (check-refs refs 2)
  (let [val (second refs)
        kw  (first refs)
        m   (update-in m [kw] replace-if-nil #{})]
    (if (set? val) (update-in m [kw] clojure.set/union val)
                   (update-in m [kw] conj val))))

(defn add-as-map [m refs]
  "Add to the current map value in a map or create a new map.
  (first refs) is the key
  (second refs) is the value: if map, merge both maps, otherwise this is a key in the second map, in which case
  (third refs) will be the value"
  (check-refs refs 2)
  (let [e2 (second refs)
        kw (first refs)
        m  (update-in m [kw] replace-if-nil {})]
    (if (map? e2)
      [(drop 2 refs) (update-in m [kw] merge e2)]
      (do (check-refs refs 3)
          (let [e3 (nth refs 2)]
            (when (not (keyword? e2)) (throw (Exception. "Map or keyword expected here")))
            [(drop 3 refs) (update-in m [kw] assoc e2 e3)])))))


(defn- throw-error [s]
  (throw (Exception.
           (str "Keyword expected here, but [" s "] found instead."))))

(defn process-references [references]
  {:pre [(even? (count references))]}
  (loop [ri {} references references]
    (if (seq references)
      (let [firstref (first references) secondref (second references)]
        (if (keyword? firstref)
          (recur (assoc ri firstref secondref) (drop 2 references))
          (throw-error firstref)))
      ri)))

(defmacro defdata [name & references]
  (let [
        ; first optional parameter is collection name
        colname    (when (string? (first references)) (first references))
        references (if colname (next references) references)
        colname    (if colname colname (str name))

        props      (process-references references)
        ]
    `(def ~name (->mongato ~colname ~props))))


(defn apply-renderinfo [object renderinfo]

  (apply assoc {} (flatten
                    (remove nil?
                      (map (
                             fn [[k v]] (let [conversion (k renderinfo)
                                              converted  (if conversion (conversion v) v)
                                              ]
                                          (if converted [k converted]))) object)))))



(defn render [m]
  "converts to readable form, according to renderer"
  (if-let [renderinfo (-> (get-mongato m) :props :renderinfo)]
    (apply-renderinfo m renderinfo)
    (if (marked-sequence? m)
      (map render m)
      m)))

(defn printm [m]
  (print (render m)))

(defn pprintm [m]
  (pprint (render m)))

(defn last4 [id]
  "Renders only the end of an id, e.g.: ..NNNN"
  (let [s (str id)]
    (str ".." (apply str (drop (- (count s) 4) s)))
    ))










