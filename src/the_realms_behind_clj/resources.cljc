(ns the-realms-behind-clj.resources
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

;; macros run in clj, during compilation
;; so cljs can use slurp
;; so long as it uses it at compilation
(defmacro inline-slurp [path]
  (clojure.core/slurp path))

(defn walk-resource [dir]
  (flatten
   (for [file (file-seq (io/file (str "resources/" dir)))
         :when (and (not (.isDirectory file))
                    (re-find #"edn$" (.getName file)))]
     (edn/read-string (inline-slurp file)))))

(def feats [] (walk-resource "feats"))
(defn equipment [] (walk-resource "equipment"))
