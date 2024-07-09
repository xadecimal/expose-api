(ns com.xadecimal.expose-api.impl-test
  (:require [com.xadecimal.expose-api.impl :as impl]
            [clojure.test :refer [deftest is]]))

(defn testivius
  "This is a
        multiline docstring."
  ([a] a)
  ([b c] [b c]))

(deftest gen-doc
  (is (= "This is a\n        multiline docstring."
         (first (impl/gen-doc (meta #'testivius))))))

(deftest gen-str
  (is (= "(defn foo [a] 'a)\n"
         (impl/gen-str
          '(defn foo [a] (quote a)))))
  (is (= "(defn foo \"One\n  Two\" [a] 'a)\n"
         (impl/gen-str
          '(defn foo "One
  Two" [a] (quote a))))))
