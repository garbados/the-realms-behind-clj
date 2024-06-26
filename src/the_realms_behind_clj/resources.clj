(ns the-realms-behind-clj.resources
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn walk-resource [dir]
  (flatten
   (for [file (file-seq (io/file (str "resources/" dir)))
         :when (and (not (.isDirectory file))
                    (re-find #"edn$" (.getName file)))]
     (edn/read-string (slurp file)))))

(def FEATS
  (delay
    (walk-resource "feats")))

(defn feats [] @FEATS)

(def EQUIPMENT
  (delay
    (walk-resource "equipment")))

(defn equipment [] @EQUIPMENT)
