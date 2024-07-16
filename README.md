# expose-api

`expose-api` is a Clojure library designed to simplify the process of creating public-facing API namespaces. In Clojure, it's common to define a namespace as your "public" interface and have implementation namespaces with the actual code. Manually writing the public namespace can be tedious. This library automates that process by generating a Clojure namespace that wraps implementation vars and exposes a public API.

## Features

- Generate a public API namespace from a specified namespace declaration and a list of implementation vars.
- Maintain consistent documentation and arities for the exposed functions and macros.
- Generates Clojure source code, leaving the runtime in a clean state.

## Installation

### Leiningen

Add the following dependency to your `project.clj`:

```clojure
[com.xadecimal/expose-api "0.3.0"]
```

### Clojure CLI/deps.edn

Add the following dependency to your `deps.edn`:

```clojure
{:deps {com.xadecimal/expose-api {:mvn/version "0.3.0"}}}
```

## Usage

### Example

Here's a quick example of how to use `expose-api` to generate a public API namespace:

```clojure
(ns your.namespace
  (:require [com.xadecimal.expose-api :as api]))

(api/expose-api
  :file-path "./src/com/xadecimal/my_lib.clj"
  :ns-code `(~'ns ~'com.xadecimal.my-lib
                "A very cool library which lets you do cool things.
                 To use it, require it and call its cool functions."
                (:refer-clojure :exclude ~'[defn])
                (:require ~'[com.xadecimal.my-lib.impl :as impl]))
  :vars [#'impl/defn #'impl/cool])
```

This will create a file `./src/com/xadecimal/my_lib.clj` containing Clojure source code of the specified namespace form, and with a function named `cool` which calls the `impl/cool` function, with the same doc and arities as that of the `impl/cool` function, as well as a macro named `defn` which calls the `impl/defn` macro, with the same doc and arities as that of the `impl/defn` macro.

### Parameters

- `file-path` - A string pointing to the path where you want the generated `.clj` namespace source file to be created. This should match the `ns-code` namespace path. It's relative to where you are executing from.
- `ns-code` - An `ns` code form, which will become the `ns` directive in the generated namespace source code. Ensure that you require the implementation namespaces that the vars use.
- `vars` - A sequence of vars that you want to wrap and expose publicly in the generated namespace. Vars are assumed to come from implementation namespaces.

## Usage as a macro

In 0.3.0, expose-api also lets you expose your vars using a macro: `expose-vars`, which won't require any source code generation. It's more straightforward, but since the generation happens at compile time, tooling that read static sources won't work with it, such as clj-kondo and clojure-lsp. Cider will pick it up when connected to a REPL.

### Example

In the namespace you want as your public API:

```clojure
(ns com.xadecimal.my-lib
"A very cool library which lets you do cool things.
 To use it, require it and call its cool functions."
  (:refer-clojure :exclude [defn])
  (:require [com.xadecimal.expose-api :refer [expose-vars]]
            [com.xadecimal.my-lib.impl :as impl]))

(expose-vars [#'impl/defn #'impl/cool])
```

You don't need a build step when using `expose-vars`, since unlike `expose-api`, it doesn't generate source code, but will instead generate at macro-expansion time.

## Comparison with Potemkin's import-vars

Potemkin's `import-vars` is another tool that facilitates the creation of public APIs by importing vars from other namespaces. Here's a comparison of `expose-api` and Potemkin's `import-vars`:

### Potemkin's import-vars

- **Usage**: Potemkin's `import-vars` is used directly in the namespace definition to import vars from other namespaces.
- **Syntax**:
  ```clojure
  (ns your.namespace
    (:require [potemkin :refer [import-vars]]
              [some.other.namespace :as other]))

  (import-vars other/foo other/bar)
  ```
- **Pros**: Simple and straightforward for importing vars.
- **Cons**: Monkey-patches things at runtime, which can lead to a less clean runtime state.

### expose-api

- **Usage**: `expose-api` generates a new namespace file with the specified public API, wrapping implementation vars.
- **Syntax**:
  ```clojure
  (api/expose-api
    :file-path "./src/com/xadecimal/my_lib.clj"
    :ns-code `(~'ns ~'com.xadecimal.my-lib
                  "A very cool library which lets you do cool things."
                  (:refer-clojure :exclude ~'[defn])
                  (:require ~'[com.xadecimal.my-lib.impl :as impl]))
    :vars [#'impl/defn #'impl/cool])
  ```
- **Pros**: Generates Clojure source code, leaving the runtime in a better state. Automates the tedious process of copying over arities and doc-strings.
- **Cons**: Requires an additional step to generate the namespace file.

### expose-vars

- **Usage**: `expose-vars` is used directly in the namespace definition to expose vars from other namespaces, same as Potemkin's `import-vars`.
- **Syntax**:
  ```clojure
  (api/expose-vars [#'impl/defn #'impl/cool])
  ```
- **Pros**: As simple and straightforward for exposing vars as Potemkin's `import-vars`, but without monkey-patching, thus it leaves the runtime in a clean state.
- **Cons**: Not friendly to static analyzers like clj-kondo or clojure-lsp.

## Build Step Integration

It is recommended to use `expose-api` from a build step. Here’s an example of how you can add such a build step using `tools.build`:

### tools.build Example

1. Create a build script (e.g., `build.clj`):

    ```clojure
    (ns build
      (:require [clojure.tools.build.api :as b]
                [com.xadecimal.expose-api :as api]))

    (defn generate-api [m]
      (api/expose-api
        :file-path "./src/com/xadecimal/my_lib.clj"
        :ns-code `(~'ns ~'com.xadecimal.my-lib
                      "A very cool library which lets you do cool things."
                      (:refer-clojure :exclude ~'[defn])
                      (:require ~'[com.xadecimal.my-lib.impl :as impl]))
        :vars [#'impl/defn #'impl/cool]))
    ```

2. Add a build alias to your `deps.edn`

    ```clojure
    :build {:extra-deps {io.github.clojure/tools.build {:git/tag "v0.10.4" :git/sha "31388ff"}}
            :extra-paths ["."]
            :ns-default build}
    ```

You need it to include your project's source whose vars are getting exposed. This is why we use `:extra-deps` and `:extra-paths`, because we will run it with `-X` and not as a tool.

3. Run the build script:

    ```shell
    clojure -X:build generate-api
    ```

This will generate the specified public API namespace as part of your build process, ensuring that your API is always up to date with the latest implementation changes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

Thanks to the Clojure community for their support and contributions to the ecosystem.
