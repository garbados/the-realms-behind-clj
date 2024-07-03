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
   :arcana      :mind
   :nature      :mind
   :presence    :spirit
   :ranged      :body
   :religion    :mind
   :resilience  :body
   :resolve     :spirit
   :society     :mind
   :sorcery     :spirit
   :stealth     :spirit
   :streetwise  :mind
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
  (let [skill (defense->skill defense)
        modifier
        (cond
          ;; parry gets a bonus from weapon defense
          (= :parry defense)
          (if-let [parry-defense
                   (->> (:equipped character)
                        (filter #(= :weapon (:slot %)))
                        (map :defense)
                        (sort >)
                        (seq))]
            (first parry-defense)
            0)
          ;; dodge gets a penalty from armor inertia
          (= :dodge defense)
          (-
           (:inertia
            (first
             (filter
              #(= :armor (:slot %))
              (:equipped character)))
            0))
          :else 0)]
    (max
     0
     (+ 8
        (character-skill character skill)
        modifier))))

(s/fdef character-defense
  :args (s/cat :character ::specs/character
               :defense ::specs/defense)
  :ret nat-int?)

(defn base-stats [character]
  (let [get-attr #(get-in character [:attributes %] 0)
        get-skill (partial character-skill character)
        get-defense (partial character-defense character)
        base-health (let [resilience (+ 2 (get-skill :resilience))]
                      [resilience (* 2 resilience)])]
    {:health base-health
     :max-health base-health
     :will (get-skill :resolve)
     :fortune (* 2 (get-attr :luck))
     :draw (* 3 (get-skill :insight))
     :speed (+ 2 (get-skill :athletics))
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
    (< level 0) (- (xp-cost base-cost (- level)))
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

(defn carried-equipment [character]
  (flatten (vals (select-keys character [:equipped :at-hand :inventory]))))

(s/fdef carried-equipment
  :args (s/cat :character ::specs/character)
  :ret ::specs/equipment)

(defn carrying-bulk [character]
  (->> (carried-equipment character)
       (map :bulk)
       (map #(if (= :light %) 0.1 %))
       (reduce +)
       (int)))

(s/fdef carrying-bulk
  :args (s/cat :character ::specs/character)
  :ret nat-int?)

(defn resolve-character [character]
  (-> character
      (update :feats (fn [feats]
                       (set
                        (map
                         (fn [feat]
                           (if (keyword? feat)
                             (resources/resolve-link feat)
                             feat))
                         feats))))
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

(def base-character
  {:bio {}
   :experience 0
   :gold 0
   :attributes
   (reduce
    #(assoc %1 %2 0)
    {}
    specs/attributes)
   :skills
   (reduce
    #(assoc %1 %2 0)
    {}
    specs/skills)
   :feats #{}
   :equipped []
   :at-hand []
   :inventory []})

(defn can-use? [character feat]
  (if (keyword? feat)
    (can-use? character (resources/resolve-link feat))
    (let [requirements (:requirements feat)
          {:keys [attributes skills feats]} requirements]
      (and
       (if attributes
         (reduce
          (fn [? [attr x]]
            (and ? (<= x (get-in character [:attributes attr] 0))))
          true
          attributes)
         true)
       (if skills
         (reduce
          (fn [? [skill x]]
            (and ? (<= x (character-skill character skill))))
          true
          skills)
         true)
       (if feats
         (let [character-feats (:feats character #{})]
           (reduce
            (fn [? feat]
              (and ? (character-feats feat)))
            true
            feats))
         true)
       (if-let [or-reqs (:or requirements)]
         (some (partial can-use? character)
               (map #(hash-map :requirements %) or-reqs))
         true)))))

(s/fdef can-use?
  :args (s/cat :character ::specs/character
               :feat ::specs/feat)
  :ret boolean?)

(defn wealth-cost [level]
  (cond
    (zero? level) 1
    (= 1 level) 3
    :else (+ (* 3 level)
             (wealth-cost (dec level)))))

(defn sort-equipment [eq1 eq2]
  (cond
    (= (:slot eq1) (:slot eq2))
    (< (:name eq1) (:name eq2))
    :else
    (< (:slot eq1) (:slot eq2))))

(defn get-max-bulk [character group]
  (let [carrying-capacity (or (get-in character [:stats :carrying-capacity])
                              (:carrying-capacity
                               (base-stats character)))]
    (case group
      :equipped carrying-capacity
      :at-hand (min carrying-capacity
                    (:stowage
                     (first
                      (filter #(= (:slot %) :belt)
                              (:equipped character)))
                     0))
      :inventory (min (* 2 carrying-capacity)
                      (:stowage
                       (first
                        (filter #(= (:slot %) :pack)
                                (:equipped character)))
                       0)))))