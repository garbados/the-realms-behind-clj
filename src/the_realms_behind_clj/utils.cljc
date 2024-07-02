(ns the-realms-behind-clj.utils 
  #?(:clj
     (:require [clojure.edn :as edn]
               [clojure.core :refer [slurp file-seq]]
               [clojure.java.io :as io])))

;; macros run in clj, during compilation
;; so cljs can use slurp
;; so long as it uses it at compilation

(defmacro walk-resource [dir]
  (flatten
   (map
    (fn [file]
      (edn/read-string (slurp file)))
    (filter
     (fn [file]
       (and (not (.isDirectory file))
            (re-find #"edn$" (.getName file))))
     (file-seq
      (io/file
       dir))))))
