(ns the-realms-behind-clj.cards-test
  (:require [clojure.test :refer [deftest testing is]]
            [the-realms-behind-clj.cards :as cards]
            [the-realms-behind-clj.specs :as specs]
            [the-realms-behind-clj.test-utils :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]))

(deftest valid-base-deck
  (testing "Base deck is valid."
    (is (s/valid? ::specs/base-deck cards/base-deck)
        (s/explain-str ::specs/base-deck cards/base-deck))))

(deftest spec-tests
  (spec-test-syms
   [`cards/remove-card
    `cards/add-card
    `cards/dancing-dream
    `cards/deal-booster-hand
    `cards/sublimate-fraction]))
