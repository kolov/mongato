(ns mongato.core
  (:require
    [monger.operators :refer :all]
    [mongato.util :refer :all]
    [clojure.pprint :refer [pprint]]
    ))




;; Type metainfo
;;  A Mongato consists of a
;;           - mongo collection name
;;           - renderinfo
(defrecord mongato [colname renderinfo])
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
        kw (first refs)
        m (update-in m [kw] replace-if-nil #{})]
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
        m (update-in m [kw] replace-if-nil {})]
    (if (map? e2)
      [(drop 2 refs) (update-in m [kw] merge e2)]
      (do (check-refs refs 3)
          (let [e3 (nth refs 2)]
            (when (not (keyword? e2)) (throw (Exception. "Map or keyword expected here")))
            [(drop 3 refs) (update-in m [kw] assoc e2 e3)])))))



(defn process-references [renderinfo references]
  (let [throw-error (fn [s] (throw (Exception. (str "One of :hide :by-type :by-name expected here, but [" s "] found instead."))))]
    (loop [ri renderinfo refs references]

      (if (not (= 0 (count refs)))
        (let [firstref (first refs)]
          (cond
            (not (keyword? firstref)) (throw-error firstref)
            ; hide expects one parameter keyword or a sequence
            (= :hide firstref) (recur (add-as-set ri refs) (drop 2 refs))
            (contains? #{:by-name :by-type} firstref) (let [[therest result] (add-as-map ri refs)] (recur result therest))
            :default (throw-error firstref)
            ))
        ri))))

(defmacro defdata [name & references]
  (let [
        ; first optional parameter is collection name
        colname (when (string? (first references)) (first references))
        references (if colname (next references) references)
        colname (if colname colname (str name))
        ; optional renderinfo
        renderinfo (when (map? (first references)) (first references))
        references (if renderinfo (next references) references)
        renderinfo (if renderinfo renderinfo {})
        ; optional renderinfo pairs/triples
        renderinfo (process-references renderinfo references)
        ]
    `(def ~name (->mongato ~colname ~renderinfo))))


(defn apply-renderinfo [object metainfo]
  (let [keys-to-hide (:hide metainfo #{})
        fn-by-name (:by-name metainfo {})
        fn-by-type (:by-type metainfo {})

        object (reduce (fn [m k] (dissoc m k))
                       object keys-to-hide)
        object (reduce (fn [m [k f]]
                         (let [curval (k m)] (if curval (assoc m k (f curval)) m)))
                       object fn-by-name)
        object (reduce (fn [m [k v]] (let [f (get fn-by-type (class v) identity)] (assoc m k (f v))))

                       {} object)

        ]

    object
    )
  )

(defn render [x]
  "converts to renderable form"
  (if-let [renderinfo (:renderinfo (get-mongato x))]
    (apply-renderinfo x renderinfo)
    (if (marked-sequence? x)
      (map render x)
      x)))

(defn printm [x]
  (print (render x)))

(defn pprintm [x]
  (pprint (render x)))







