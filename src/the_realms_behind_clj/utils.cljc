(ns the-realms-behind-clj.utils)

;; macros run in clj, during compilation
;; so cljs can use slurp
;; so long as it uses it at compilation

(defmacro inline-slurp [path]
  (clojure.core/slurp path))

(defmacro walk-resource [dir]
  (flatten
   (map
    (fn [file]
      (clojure.edn/read-string (clojure.core/slurp file)))
    (filter
     (fn [file]
       (and (not (.isDirectory file))
            (re-find #"edn$" (.getName file))))
     (clojure.core/file-seq
      (clojure.java.io/file
       dir))))))
