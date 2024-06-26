(ns the-realms-behind-clj.resources-test
  (:require [clojure.test :refer [deftest testing is]]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.specs :as specs]
            [clojure.spec.alpha :as s]))

(deftest valid-resources
  (testing "Feats resources contain valid definitions."
    (let [feats (resources/feats)]
      (is (s/valid? ::specs/feats feats)
          (s/explain-str ::specs/feats feats))))
  (testing "Equipment resources contain valid definitions."
    (let [equipment (resources/equipment)]
      (is (s/valid? ::specs/equipment equipment)
          (s/explain-str ::specs/equipment equipment)))))
