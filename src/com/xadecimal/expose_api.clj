(ns com.xadecimal.expose-api
  (:require [com.xadecimal.expose-api.impl :as impl]
            [clojure.string :as str]))

(defn expose-api
  "Generate a public facing API namespace.

   Takes a map of:

   * file-path - a string pointing to the path where you want the generated .clj
                 namespace source file to be created, should match the ns-code
                 namespace path. Relative to where you are executing from.
   * ns-code - An ns code form, which will become the ns directive in the
               generated namespace source code. Make sure to require the impl
               namespaces the vars make use of.
   * vars - A seqable of vars which you want wrapped and exposed publicly in
            the generated namespace. Vars are assume to come from impl
            namespaces.

   Example:
     (expose-api :file-path \"./src/com/xadecimal/my_lib.clj\"
                 :ns-code `(~'ns ~'com.xadecimal.my-lib
                            \"A very cool library which let's you do cool things.
                              To use it, require it and call it's cool functions.\"
                            (:refer-clojure :exclude ~'[defn])
                            (:require ~'[com.xadecimal.my-lib.impl :as impl]))
                 :vars [#'impl/defn #'impl/cool])

     This will create the file ./src/com/xadecimal/my_lib.clj which will
     contain Clojure source code of the specified ns form, and with a function
     named cool which calls the impl/cool function, with the same doc and
     arities as that of the impl/cool function, as well as a macro named defn
     which calls the impl/defn macro, with the same doc and arities as that of
     the impl/defn macro."
  [& {:keys [file-path ns-code vars]}]
  (let [ns-str (impl/gen-str ns-code)
        vars (mapv #(vector % []) vars)
        top-level-forms (mapv #(impl/gen-top-level (first %) (second %)) vars)]
    (spit file-path
          (str/join \newline (into [ns-str] top-level-forms)))))
