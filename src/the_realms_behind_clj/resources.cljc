(ns the-realms-behind-clj.resources
  (:require [the-realms-behind-clj.utils
             :include-macros true
             :refer [walk-resource]]))

(def CHARACTERS (macroexpand-1 '(walk-resource "resources/characters")))
(def FEATS (macroexpand-1 '(walk-resource "resources/feats")))
(def EQUIPMENT (macroexpand-1 '(walk-resource "resources/equipment")))

(defn characters [] CHARACTERS)
(defn feats [] FEATS)
(defn equipment [] EQUIPMENT)

(def id->content
  (let [content (concat (feats)
                        (equipment))
        ids (map :id content)]
    (zipmap ids content)))

;; hash-map acts as resolver
(def resolve-link id->content)
