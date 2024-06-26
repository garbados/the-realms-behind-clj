(defproject the-realms-behind-clj "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]]
  :repl-options {:init-ns the-realms-behind-clj.core}
  :plugins [[lein-cloverage "1.2.2"]]
  :profiles
  {:dev {:dependencies [[org.clojure/test.check "1.1.1"]]}
   :cljs
   {:source-paths ["src" "test"]
    :dependencies [[thheller/shadow-cljs "2.26.4"]
                   [reagent "1.2.0"]]}})
