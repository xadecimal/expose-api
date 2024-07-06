(ns com.xadecimal.expose-api.impl
  (:require [zprint.core :as zp]))

(defn gen-doc
  [mta]
  (when-let [doc (:doc mta)] [doc]))

(defn gen-norm-parameters
  [norm-args splice?]
  (mapv #(if splice? (symbol (str "~" %)) %) norm-args))

(defn gen-rest-parameters
  [norm-args rest-arg splice?]
  (conj
   (mapv #(if splice? (symbol (str "~" %)) %) norm-args)
   (if splice? (symbol (str "~@" rest-arg)) rest-arg)))

(defn gen-call
  [vr args macro?]
  (let [[norm-args [_ rest-arg]] (split-with (complement #{'&}) args)]
    (if (nil? rest-arg)
      (if macro?
        (cons (symbol "`") `((~(symbol vr) ~@(gen-norm-parameters norm-args true))))
        [`(~(symbol vr) ~@(gen-norm-parameters norm-args false))])
      (if macro?
        (cons (symbol "`") `((~(symbol vr) ~@(gen-rest-parameters norm-args rest-arg true))))
        [`(apply ~(symbol vr) ~@(gen-rest-parameters norm-args rest-arg false))]))))

(defn gen-macro-call
  [vr mta]
  (for [args (:arglists mta)]
    `(~args
      ~@(gen-call vr args true))))

(defn gen-macro
  [vr mta copy-meta]
  `(~'defmacro ~(:name mta)
    ~@(gen-doc mta)
    ~(select-keys mta copy-meta)
    ~@(gen-macro-call vr mta)))

(defn gen-defn
  [vr mta copy-meta]
  `(~'defn ~(:name mta)
    ~@(gen-doc mta)
    ~(assoc (select-keys mta copy-meta)
            :inline `(~'fn ~@(gen-macro-call vr mta)))
    ~@(for [args (:arglists mta)]
        `(~args
          ~@(gen-call vr args false)))))

(defn gen-str
  [forms]
  (with-out-str
    (zp/zprint forms)))

(defn gen-top-level
  [vr copy-meta]
  (gen-str
   (let [mta (meta vr)]
     (if (:macro mta)
       (gen-macro vr mta copy-meta)
       (gen-defn vr mta copy-meta)))))
