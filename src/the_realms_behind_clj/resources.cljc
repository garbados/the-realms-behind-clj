(ns the-realms-behind-clj.resources
  (:require [the-realms-behind-clj.utils
             :include-macros true
             :refer [walk-resource]]))

(def CHARACTERS (macroexpand-1 '(walk-resource "resources/characters")))
(def FEATS (macroexpand-1 '(walk-resource "resources/feats")))
(def EQUIPMENT (macroexpand-1 '(walk-resource "resources/equipment")))
(def FEATURES (macroexpand-1 '(walk-resource "resources/features")))

(defn characters [] CHARACTERS)
(defn feats [] FEATS)
(defn equipment [] EQUIPMENT)
(defn features [] FEATURES)

(def id->content
  (let [content (concat (characters)
                        (feats)
                        (equipment)
                        (features))
        ids (map :id content)]
    (zipmap ids content)))

;; hash-map acts as resolver
(def resolve-link id->content)
