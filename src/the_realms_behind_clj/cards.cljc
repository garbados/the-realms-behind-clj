(ns the-realms-behind-clj.cards
  (:require [clojure.spec.alpha :as s]
            [the-realms-behind-clj.specs :as specs]))

(def base-deck
  (flatten
   (for [suit specs/suits]
     (for [rank (range 1 14)]
       {:suit suit :rank rank}))))

(defn remove-card [deck card]
  (let [n (count (keep #{card} deck))]
    (cond
      (zero? n) deck
      (= 1 n) (remove #{card} deck)
      (< 1 n) (reduce
               (fn [deck _]
                 (conj deck card))
               (remove #{card} deck)
               (range (dec n))))))

(s/fdef remove-card
  :args (s/cat :deck ::specs/deck
               :card ::specs/card)
  :ret ::specs/deck)

(defn add-card [deck card]
  (conj deck card))

(s/fdef add-card
  :args (s/cat :deck ::specs/deck
               :card ::specs/card)
  :ret ::specs/deck)

(defn dancing-dream [fq-deck pc-deck card]
  [(add-card fq-deck card)
   (remove-card pc-deck card)])

(s/fdef dancing-dream
  :args (s/cat :fq-deck ::specs/deck
               :pc-deck ::specs/deck
               :card ::specs/card)
  :ret (s/tuple ::specs/deck ::specs/deck))

(defn deal-booster-hand [deck]
  (take 10 (shuffle deck)))

(s/fdef deal-booster-hand
  :args (s/cat :deck ::specs/deck)
  :ret (s/coll-of ::specs/card))

(defn sublimate-fraction [fq-deck pc-deck card]
  [(remove-card fq-deck card)
   (add-card pc-deck card)])

(s/fdef sublimate-fraction
  :args (s/cat :fq-deck ::specs/deck
               :pc-deck ::specs/deck
               :card ::specs/card)
  :ret (s/tuple ::specs/deck ::specs/deck))
