(ns the-realms-behind-clj.core
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.spec.alpha :as s]))

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

(s/def ::id keyword?)
(s/def ::name string?)
(s/def ::description string?)
(s/def ::tags set?)

(s/def ::base-definition
  (s/keys :req-un [::id
                   ::name
                   ::description]
          :opt-un [::tags]))

(s/def ::gain-feats
  (s/coll-of keyword?))
(s/def ::gain-items
  (s/coll-of keyword?))
(s/def ::choose-items
  (s/tuple nat-int? set?))
(s/def ::when-gained
  (s/keys :opt-un [::gain-feats
                   ::gain-items
                   ::choose-items]))

(s/def ::feat
  (s/and ::base-definition
         (s/keys :opt-un [::when-gained
                          ::effect
                          ::requirements])))

(s/def ::feats (s/coll-of ::feat))

(s/def ::type #{:weapon :armor :pack :item})
(s/def ::level nat-int?)
(s/def ::base-equipment
  (s/and ::base-definition
         (s/keys :req-un [::type
                          ::level])))
(s/def ::accuracy nat-int?)
(s/def ::damage nat-int?)
(s/def ::defense nat-int?)
(s/def ::range-expr
  (s/or :melee keyword?
        :ranged nat-int?
        :varied (s/tuple keyword? nat-int?)))
(s/def ::range
  (s/or :expr ::range-expr
        :exprs (s/coll-of ::range-expr)))
(s/def ::might nat-int?)
(s/def ::enchantments
  (s/keys :req-un []))
(s/def ::weapon
  (s/and ::base-equipment
         (s/keys :req-un [::accuracy
                          ::damage
                          ::defense
                          ::range
                          ::might]
                 :opt-un [::enchantments])))
(s/def ::elements #{:physical :fire :frost :brilliance :shadow})
(s/def ::resists (s/map-of ::elements nat-int?))
(s/def ::inertia nat-int?)
(s/def ::armor
  (s/and ::base-equipment
         (s/keys :req-un [::resists
                          ::inertia
                          ::might]
                 :opt-un [::enchantments])))
(s/def ::encumbrance nat-int?)
(s/def ::pack
  (s/and ::base-equipment
         (s/keys :req-un [::might
                          ::encumbrance
                          ::enchantments])))
(s/def ::item ::base-equipment)
(s/def ::equipment
  (s/coll-of
   (s/or :weapon ::weapon
         :armor ::armor
         :pack ::pack
         :item ::item)))
