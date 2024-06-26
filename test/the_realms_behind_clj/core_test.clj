(ns the-realms-behind-clj.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [the-realms-behind-clj.core :as core]
            [clojure.spec.alpha :as s]))

(deftest valid-resources
  (testing "Feats resources contain valid definitions."
    (let [feats (core/feats)]
      (is (s/valid? ::core/feats feats)
          (s/explain-str ::core/feats feats))))
  (testing "Equipment resources contain valid definitions."
    (let [equipment (core/equipment)]
      (is (s/valid? ::core/equipment equipment)
          (s/explain-str ::core/equipment equipment)))))
