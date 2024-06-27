(defproject the-realms-behind-clj "1.0.0"
  :description "A d13 tabletop roleplaying game, with tooling!"
  :url "https://garbados.github.io/the-realms-behind-clj/"
  :license {:name "CC BY-NC-SA 4.0"
            :url "https://creativecommons.org/licenses/by-nc-sa/4.0/deed.en"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns the-realms-behind-clj.core}
  :plugins [[lein-cloverage "1.2.2"]]
  :profiles
  {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}
   :cljs
   {:source-paths ["src" "test"]
    :dependencies [[thheller/shadow-cljs "2.26.4"]
                   [reagent "1.2.0"]
                   [metosin/reitit "0.7.0-alpha7"]
                   [metosin/reitit-spec "0.7.0-alpha7"]
                   [metosin/reitit-frontend "0.7.0-alpha7"]]}})
