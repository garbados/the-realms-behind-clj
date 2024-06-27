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
  (if (> 2 level)
    base-cost
    (+ (* level base-cost) (xp-cost base-cost (dec level)))))

(s/fdef xp-cost
  :args (s/cat :cost nat-int?
               :level nat-int?)
  :ret nat-int?)

(defn base-xp [{:keys [attributes skills feats]}]
  (->>
   [(map #(xp-cost 2 %) (vals attributes))
    (map #(xp-cost 1 %) (vals skills))
    (map #(xp-cost 3 %)
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

(def sample-characters
  {:dhutlo
   {:bio
    {:name "Dhutlo"
     :description "A hardy lizard."
     :player "DFB"
     :image-url "https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fstevelees.photography%2Fwp-content%2Fuploads%2FSteve-Lees-Photography-Australian-Lizard-3.jpg&f=1&nofb=1&ipt=a0fd2cd1803e6eb766d5d8d908643e07cb24c9b06e858e0f469390c7fa091438&ipo=images"}
    :experience 0
    :attributes
    {:body 3
     :mind 1
     :spirit 2
     :luck 2}
    :skills
    {:acrobatics 1
     :athletics 2
     :awareness 1
     :insight 1
     :medicine 2
     :melee 3
     :might 2
     :presence 2
     :resilience 2
     :resolve 1
     :stealth 1
     :survival 1
     :theurgy 2}
    :feats
    #{:extra-hearts-damage
      :extra-hearts-healing}
    :equipped [(resources/resolve-link :innate-weapon)
               (resources/resolve-link :medium-armor)]
    :at-hand []
    :inventory []}})