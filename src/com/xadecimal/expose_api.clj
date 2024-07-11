(ns com.xadecimal.expose-api
  (:require [com.xadecimal.expose-api.impl :as impl]))

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
        top-level-forms-str (mapv #(impl/gen-top-level-str (first %) (second %)) vars)]
    (impl/spit-generated-file file-path ns-str top-level-forms-str)))

(defmacro expose-vars
  "Use in an existing namespace, will defn or defmacro wrapper functions or
   macros that have the same signature and doc-string as the ones from the
   given vars, and which internally delegates to the vars fn/macro.

   Unlike expose-api, this does not perform source code generation, but is
   meant to be used as a macro. Making it more convenient, but it won't expose
   things in a way that is friendly to human reader or static analysis tools.

   Take a seqable of vars:

   * vars - A seqable of vars which you want wrapped and exposed publicly in
            the current namespace. Vars are assume to come from impl
            namespaces.

   Example:
     (ns foo
       (:require [com.xadecimal.expose-api :refer [expose-vars]]))

     (expose-vars [#'impl/defn #'impl/cool])

   This will define a macro called defn and a function called cool in the foo
   namespace which have the same arity, signature and doc-string as impl/defn
   and impl/cool. When called, they will internally delegate to impl/defn and
   impl/cool."
  [vars]
  (let [vars (mapv #(vector (requiring-resolve (second %)) []) vars)
        top-level-forms (->> vars
                             (map #(impl/gen-top-level-str (first %) (second %)))
                             (mapv read-string))]
    `(do ~@top-level-forms)))
