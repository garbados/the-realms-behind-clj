(ns the-realms-behind-clj.specs
  (:require [clojure.spec.alpha :as s]))

(s/def ::id keyword?)
(s/def ::links (s/coll-of ::id))
(s/def ::name string?)
(s/def ::description string?)
(s/def ::tags (s/coll-of keyword? :kind set?))
(s/def ::level int?)

;; BASIC SPECS

(def attributes #{:body :mind :spirit :luck})
(s/def ::attribute attributes)
(s/def ::attributes (s/map-of ::attribute nat-int?))

(def skills #{:acrobatics
              :arcana
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
              :nature
              :performance
              :presence
              :ranged
              :religion
              :resilience
              :resolve
              :society
              :sorcery
              :stealth
              :theurgy
              :thievery})
(s/def ::skill skills)

(s/def ::skills (s/map-of ::skill nat-int?))

(s/def ::ap nat-int?)
(s/def ::madness nat-int?)

(s/def ::damage-expr
  (s/or :raw nat-int?
        :with-attr (s/tuple ::attribute nat-int?)))
(s/def ::damage
  (s/or :raw ::damage-expr
        :with-element (s/map-of ::element ::damage-expr)))
(s/def ::range-expr
  (s/or :melee #{:close}
        :ranged nat-int?
        :varied (s/map-of ::attribute nat-int?)))
(s/def ::range
  (s/or :expr ::range-expr
        :exprs (s/coll-of ::range-expr)))

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
                   ::description
                   ::level]
          :opt-un [::tags]))

(s/def ::gain-feats ::links)
(s/def ::gain-items ::links)

(s/def ::checks
  (s/coll-of
   (s/or :factor keyword?
         :best-of (s/coll-of keyword?))
   :count 2))
(s/def ::cost
  (s/keys :req-un [::ap]
          :opt-un [::madness]))
(s/def ::base-action
  (s/keys :opt-un [::checks
                   ::cost
                   ::damage
                   ::range]))
(s/def ::action
  (s/merge ::base-definition
           ::base-action))

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
           ::base-action
           (s/keys :opt-un [::when-gained
                            ::effect
                            ::requirements])))

(s/def ::feats (s/coll-of ::feat))

(def materials #{:cloth :wood :leather :bone :metal
                 :food :medicine
                 :precious
                 :fire :frost :radiant :shadow})
(s/def ::materials (s/coll-of materials))
(s/def ::bulk (s/or :light #{:light}
                    :nat-int nat-int?))
(s/def ::base-equipment
  (s/merge ::base-definition
           (s/keys :req-un [::bulk
                            ::materials])))
(s/def ::accuracy nat-int?)
(s/def ::might nat-int?)
(s/def ::enhancement
  (s/merge ::base-definition
           (s/keys :opt-un [::materials
                            ::effect])))
(s/def ::enhancements (s/coll-of ::enhancement))
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
                   :opt-un [::enhancements])))
(s/def ::element #{:physical :fire :frost :radiant :shadow})
(s/def ::resists (s/map-of ::element nat-int?))
(s/def ::inertia nat-int?)
(s/def :armor/slot #{:armor})
(s/def ::armor
  (s/merge ::base-equipment
           (s/keys :req-un [:armor/slot
                            ::resists
                            ::inertia
                            ::might]
                   :opt-un [::enhancements])))
(s/def ::stowage nat-int?)
(s/def :storage/slot #{:belt :pack})
(s/def ::storage
  (s/merge ::base-equipment
           (s/keys :req-un [:storage/slot
                            ::might
                            ::stowage]
                   :opt-un [::enhancements])))
(s/def :item/slot #{:item})
(s/def ::item
  (s/merge ::base-equipment
           (s/keys :req-un [:item/slot])))
(def equippable-slots
  #{:weapon :armor :shield :belt :pack
    :head :torso :hands :feet :ring})
(def slots (into equippable-slots #{:item}))
(s/def ::slot slots)
(s/def :clothing/slot #{:head :torso :hands :feet :ring})
(s/def ::clothing
  (s/merge ::base-equipment
           (s/keys :req-un [:clothing/slot
                            ::enhancements
                            ::might])))
(s/def :shield/slot #{:shield})
(s/def ::shield
  (s/merge ::base-equipment
           (s/keys :req-un [:shield/slot
                            ::resists
                            ::damage
                            :weapon/defense
                            ::inertia
                            ::might]
                   :opt-un [::enhancements])))
(s/def ::single-equipment
  (s/or :weapon ::weapon
        :armor ::armor
        :shield ::shield
        :clothing ::clothing
        :storage ::storage
        :item ::item))
(s/def ::equipment (s/coll-of ::single-equipment))

(s/def ::feature ::base-definition)
(s/def ::features (s/coll-of ::feature))

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
(s/def ::gold nat-int?)
(s/def :character/feats
  (s/coll-of
   (s/or :detail ::feat
         :link   ::id)
   :kind set?))
(s/def ::character
  (s/keys :req-un [::bio
                   ::experience
                   ::gold
                   ::attributes
                   ::skills
                   :character/feats
                   ::equipped
                   ::at-hand
                   ::inventory]
          :opt-un [::stats]))
