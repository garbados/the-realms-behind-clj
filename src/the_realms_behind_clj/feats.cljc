(ns the-realms-behind-clj.feats
  (:require [clojure.spec.alpha :as s]
            [the-realms-behind-clj.specs :as specs]))

(defn feats+tag=>feats [feats & tags]
  (filter (comp (partial every? some?) (apply juxt tags) :tags) feats))

(s/fdef feats+tag=>feats
  :args (s/cat :feats ::specs/feats
               :tags (s/+ keyword?))
  :ret ::specs/feats)

(defn feats-tag=>feats [feats & tags]
  (filter (comp (partial every? nil?) (apply juxt tags) :tags) feats))

(s/fdef feats-tag=>feats
  :args (s/cat :feats ::specs/feats
               :tags (s/+ keyword?))
  :ret ::specs/feats)
