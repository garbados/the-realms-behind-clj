(ns the-realms-behind-clj.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::id keyword?)
(s/def ::name string?)
(s/def ::description string?)
(s/def ::tags set?)

;; CONTENT DEFINITIONS

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

;; CHARACTER DEFINITIONS

(def attributes #{:body :mind :spirit :luck})
(s/def ::attribute attributes)

(def skills #{:acrobatics
              :athletics
              :awareness
              :craft
              :deception
              :diplomacy
              :insight
              :lore
              :medicine
              :melee
              :might
              :performance
              :presence
              :ranged
              :resilience
              :resolve
              :sorcery
              :stealth
              :streetwise
              :survival
              :theurgy})
(s/def ::skill skills)

(s/def ::attributes (s/map-of ::attribute nat-int?))
(s/def ::skills (s/map-of ::skill nat-int?))

(s/def ::equipped ::equipment)
(s/def ::inventory ::equipment)

(s/def ::health
  (s/tuple nat-int? nat-int?))
(s/def ::will nat-int?)
(s/def ::fortune nat-int?)
(s/def ::draw nat-int?)
(s/def ::speed nat-int?)
(s/def ::initiative nat-int?)

(s/def ::parry nat-int?)
(s/def ::dodge nat-int?)
(s/def ::body nat-int?)
(s/def ::mind nat-int?)
(s/def ::spirit nat-int?)
(s/def ::luck nat-int?)
(s/def ::defenses
  (s/keys :req-un [::parry
                   ::dodge
                   ::body
                   ::mind
                   ::spirit
                   ::luck]))

(s/def ::stats
  (s/keys :req-un [::health
                   ::will
                   ::fortune
                   ::draw
                   ::speed
                   ::initiative
                   ::defenses]))

(s/def ::player string?)
(s/def ::bio
  (s/keys :req-un [::name
                   ::player
                   ::description]))

(s/def ::effects (s/map-of keyword? any?))

(s/def ::character
  (s/keys :req-un [::bio
                   ::effects
                   ::attributes
                   ::skills
                   ::feats
                   ::equipped
                   ::inventory
                   ::stats]))

(s/def ::defense
  (s/or :attribute ::attribute
        :melee #{:parry :dodge}))
