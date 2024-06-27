(ns the-realms-behind-clj.resources
  (:require [the-realms-behind-clj.utils
             :include-macros true
             :refer [walk-resource]]))

(def FEATS `(walk-resource "resources/feats"))
(def EQUIPMENT `(walk-resource "resources/equipment"))

(defn feats [] FEATS)
(defn equipment [] EQUIPMENT)
