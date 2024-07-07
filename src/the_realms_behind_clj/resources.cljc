(ns the-realms-behind-clj.resources
  (:require [the-realms-behind-clj.utils
             :include-macros true
             :refer [walk-resource]]))

(def ACTIONS (macroexpand-1 '(walk-resource "resources/actions")))
(def CHARACTERS (macroexpand-1 '(walk-resource "resources/characters")))
(def ENHANCEMENTS (macroexpand-1 '(walk-resource "resources/enhancements")))
(def FEATS (macroexpand-1 '(walk-resource "resources/feats")))
(def EQUIPMENT (macroexpand-1 '(walk-resource "resources/equipment")))
(def FEATURES (macroexpand-1 '(walk-resource "resources/features")))

(defn actions [] ACTIONS)
(defn characters [] CHARACTERS)
(defn enhancements [] ENHANCEMENTS)
(defn feats [] FEATS)
(defn equipment [] EQUIPMENT)
(defn features [] FEATURES)

(def all-content
  (concat (actions)
          (enhancements)
          (feats)
          (equipment)
          (features)))

(def id->content
  (let [content all-content
        ids (map :id content)]
    (zipmap ids content)))

(def name->content
  (reduce
   (fn [name->content content]
     (assoc name->content (:name content) content))
   {}
   (vals id->content)))

;; hash-map acts as resolver
(def resolve-link id->content)
