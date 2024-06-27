(ns the-realms-behind-clj.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::id keyword?)
(s/def ::links (s/coll-of ::id))
(s/def ::name string?)
(s/def ::description string?)
(s/def ::tags (s/coll-of keyword? :kind set?))

;; BASIC SPECS

(def attributes #{:body :mind :spirit :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute nat-int?))

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

(s/def ::skills (s/map-of ::skill nat-int?))

;; CARD SPECS

(def suits #{:hearts :spades :clubs :diamonds})
(s/def ::suit suits)
(s/def ::rank (s/int-in 1 14))
(s/def ::card
  (s/keys :req-un [::suit
                   ::rank]))

(s/def ::base-deck (s/coll-of ::card :distinct true :count 52))
(s/def ::deck (s/coll-of ::card))

;; CONTENT DEFINITION SPECS

(s/def ::base-definition
  (s/keys :req-un [::id
                   ::name
                   ::description]
          :opt-un [::tags]))

(s/def ::gain-feats ::links)
(s/def ::gain-items ::links)
(s/def ::choose-items
  (s/tuple nat-int? set?))
(s/def ::when-gained
  (s/keys :opt-un [::gain-feats
                   ::gain-items
                   ::choose-items]))

(s/def ::extra-suit-damage ::suit)
(s/def ::extra-suit-healing ::suit)
(s/def ::keen-attribute ::attribute)
(s/def ::deep-debuffs ::suit)
(s/def ::use-skill-as (s/map-of ::skill ::skill))
(s/def ::effect
  (s/keys :opt-un [::extra-suit-damage
                   ::extra-suit-healing
                   ::keen-attribute
                   ::deep-debuffs
                   ::use-skill-as]))

(s/def ::requirements
  (s/keys :opt-un [::attributes
                   ::skills
                   :character/feats]))

(s/def ::feat
  (s/merge ::base-definition
           (s/keys :opt-un [::when-gained
                            ::effect
                            ::requirements])))

(s/def ::feats (s/coll-of ::feat))

(s/def ::level nat-int?)
(s/def ::bulk (s/or :light #{:light}
                    :nat-int nat-int?))
(s/def ::base-equipment
  (s/merge ::base-definition
           (s/keys :req-un [::level
                            ::bulk])))
(s/def ::accuracy nat-int?)
(s/def ::damage nat-int?)
(s/def ::range-expr
  (s/or :melee #{:close}
        :ranged nat-int?
        :varied (s/tuple ::attribute nat-int?)))
(s/def ::range
  (s/or :expr ::range-expr
        :exprs (s/coll-of ::range-expr)))
(s/def ::might nat-int?)
(s/def ::enchantments
  (s/keys :req-un []))
(s/def :weapon/slot #{:weapon})
(s/def :weapon/defense nat-int?)
(s/def ::weapon
  (s/merge ::base-equipment
           (s/keys :req-un [:weapon/slot
                            ::accuracy
                            ::damage
                            :weapon/defense
                            ::range
                            ::might]
                   :opt-un [::enchantments])))
(s/def ::elements #{:physical :fire :frost :brilliance :shadow})
(s/def ::resists (s/map-of ::elements nat-int?))
(s/def ::inertia nat-int?)
(s/def :armor/slot #{:armor})
(s/def ::armor
  (s/merge ::base-equipment
           (s/keys :req-un [:armor/slot
                            ::resists
                            ::inertia
                            ::might]
                   :opt-un [::enchantments])))
(s/def ::stowage nat-int?)
(s/def :storage/slot #{:belt :pack})
(s/def ::storage
  (s/merge ::base-equipment
           (s/keys :req-un [:storage/slot
                            ::might
                            ::stowage]
                   :opt-un [::enchantments])))
(s/def :item/slot #{:item})
(s/def ::item
  (s/merge ::base-equipment
           (s/keys :req-un [:item/slot])))
(def equippable-slots
  #{:weapon :armor :shield :belt :pack
    :neck :torso :arms :legs :hands :feet
    :jewelry})
(def slots (into equippable-slots #{:item}))
(s/def ::slot slots)
(s/def ::equipment-single
  (s/or :weapon ::weapon
        :armor ::armor
        :storage ::storage
        :item ::item))
(s/def ::equipment (s/coll-of ::equipment-single))

;; CHARACTER SPECS

(s/def ::equipped ::equipment)
(s/def ::at-hand ::equipment)
(s/def ::inventory ::equipment)

(s/def ::health
  (s/tuple nat-int? nat-int?))
(s/def ::will nat-int?)
(s/def ::fortune nat-int?)
(s/def ::draw nat-int?)
(s/def ::speed nat-int?)
(s/def ::initiative nat-int?)
(s/def ::defense
  (s/or :attribute ::attribute
        :melee #{:parry :dodge}))
(s/def ::defenses
  (s/map-of ::defense nat-int?))
(s/def ::carrying-capacity nat-int?)
(s/def ::madness nat-int?)
(s/def ::stats
  (s/keys :req-un [::health
                   ::will
                   ::fortune
                   ::draw
                   ::speed
                   ::initiative
                   ::defenses
                   ::carrying-capacity
                   ::madness]))

(s/def ::player string?)
(s/def ::image-url string?)
(s/def ::bio
  (s/keys :req-un [::name
                   ::player
                   ::description
                   ::image-url]))

(s/def ::experience nat-int?)
(s/def :character/feats ::links)
(s/def ::character
  (s/keys :req-un [::bio
                   ::experience
                   ::attributes
                   ::skills
                   :character/feats
                   ::equipped
                   ::at-hand
                   ::inventory]
          :opt-un [::stats]))
