(ns the-realms-behind-clj.characters
  (:require [clojure.spec.alpha :as s]
            [the-realms-behind-clj.specs :as specs]))

(def attribute->suit
  {:body   :hearts
   :mind   :spades
   :spirit :clubs
   :luck   :diamonds})

(def skill->attribute
  {:acrobatics  :body
   :athletics   :body
   :awareness   :spirit
   :craft       :mind
   :deception   :mind
   :diplomacy   :mind
   :insight     :mind
   :lore        :mind
   :medicine    :mind
   :melee       :body
   :might       :body
   :performance :spirit
   :presence    :spirit
   :ranged      :body
   :resilience  :body
   :resolve     :spirit
   :sorcery     :spirit
   :stealth     :spirit
   :streetwise  :mind
   :survival    :mind
   :theurgy     :luck})

(defn character-skill [character skill]
  (let [attribute (skill->attribute skill)]
    (+ (get-in character [:attributes attribute] 0)
       (get-in character [:skills skill] 0))))

(s/fdef character-skill
  :args (s/cat :character ::specs/character
               :skill ::specs/skill)
  :ret nat-int?)

(def defense->skill
  {:parry :melee
   :dodge :acrobatics
   :body :resilience
   :mind :insight
   :spirit :resolve
   :luck :theurgy})

(defn character-defense [character defense]
  (let [skill (defense->skill defense)]
    (character-skill character skill)))

(s/fdef character-defense
  :args (s/cat :character ::specs/character
               :defense ::specs/defense)
  :ret nat-int?)

(defn base-stats [character]
  (let [get-attr #(get-in character [:attributes %] 0)
        get-skill (partial character-skill character)
        get-defense (partial character-defense character)]
    {:health
     (let [resilience (get-skill :resilience)]
       [resilience (* 2 resilience)])
     :will (get-skill :resolve)
     :fortune (* 2 (get-attr :luck))
     :draw (* 3 (get-skill :insight))
     :speed (* 2 (get-skill :athletics))
     :initiative (* 2 (get-skill :awareness))
     :defenses
     (reduce
      (fn [defenses defense]
        (assoc defenses defense (get-defense defense)))
      {}
      (keys defense->skill))
     :madness 0}))

(s/fdef base-stats
  :args (s/cat :character ::specs/character)
  :ret ::specs/stats)

(defn group-equipment [equipment]
  (group-by :slot equipment))

(s/fdef group-equipment
  :args (s/cat :equipment ::specs/equipment)
  :ret (s/map-of ::specs/slot ::specs/equipment))
