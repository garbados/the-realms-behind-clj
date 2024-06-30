(ns the-realms-behind-clj.characters
  (:require [clojure.spec.alpha :as s]
            [the-realms-behind-clj.resources :as resources]
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
    (+ 8 (character-skill character skill))))

(s/fdef character-defense
  :args (s/cat :character ::specs/character
               :defense ::specs/defense)
  :ret nat-int?)

(defn base-stats [character]
  (let [get-attr #(get-in character [:attributes %] 0)
        get-skill (partial character-skill character)
        get-defense (partial character-defense character)
        base-health (let [resilience (get-skill :resilience)]
                      [resilience (* 2 resilience)])]
    {:health base-health
     :max-health base-health
     :will (get-skill :resolve)
     :fortune (* 2 (get-attr :luck))
     :draw (* 3 (get-skill :insight))
     :speed (get-skill :athletics)
     :initiative (* 2 (get-skill :awareness))
     :defenses
     (reduce
      (fn [defenses defense]
        (assoc defenses defense (get-defense defense)))
      {}
      (keys defense->skill))
     :carrying-capacity (+ 3 (get-skill :might))
     :madness 0}))

(s/fdef base-stats
  :args (s/cat :character ::specs/character)
  :ret ::specs/stats)

(defn xp-cost [base-cost level]
  (cond
    (zero? level) 0
    (= 1 level) base-cost
    :else
    (+ (* level base-cost) (xp-cost base-cost (dec level)))))

(s/fdef xp-cost
  :args (s/cat :cost nat-int?
               :level nat-int?)
  :ret nat-int?)

(def attr-xp-cost (partial xp-cost 2))
(def skill-xp-cost (partial xp-cost 1))
(def feat-xp-cost (partial xp-cost 3))


(defn base-xp [{:keys [attributes skills feats]}]
  (->>
   [(map attr-xp-cost (vals attributes))
    (map skill-xp-cost (vals skills))
    (map feat-xp-cost
         (->> feats
              (map (comp :level resources/resolve-link))
              (filter some?)))]
   (flatten)
   (reduce + 0)))

(s/fdef base-xp
  :args (s/cat :character ::specs/character)
  :ret nat-int?)

(defn carrying [character]
  (flatten (vals (select-keys character [:equipped :at-hand :inventory]))))

(s/fdef carrying
  :args (s/cat :character ::specs/character)
  :ret ::specs/equipment)

(defn carrying-bulk [character]
  (->> (carrying character)
       (map :bulk)
       (map #(if (= :light %) 0.1 %))
       (reduce +)
       (int)))

(s/fdef carrying-bulk
  :args (s/cat :character ::specs/character)
  :ret nat-int?)

(defn resolve-character [character]
  (-> character
      (update :equipped #(map resources/resolve-link %))
      (update :at-hand #(map resources/resolve-link %))
      (update :inventory #(map resources/resolve-link %))))

(def sample-characters
  (->> (resources/characters)
       (map resolve-character)
       (reduce
        (fn [all character]
          (assoc all (:id character) character))
        {})))
