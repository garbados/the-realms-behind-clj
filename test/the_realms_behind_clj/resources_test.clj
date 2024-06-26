(ns the-realms-behind-clj.resources-test
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.specs :as specs]))

(deftest valid-resources
  (testing "Feats resources contain valid definitions."
    (let [feats (resources/feats)]
      (is (s/valid? ::specs/feats feats)
          (s/explain-str ::specs/feats feats))))
  (testing "Equipment resources contain valid definitions."
    (let [equipment (resources/equipment)]
      (is (s/valid? ::specs/equipment equipment)
          (s/explain-str ::specs/equipment equipment))))
  (testing "Unique IDs across content"
    (let [dfns (concat (resources/feats)
                       (resources/equipment))
          ids (map :id dfns)
          uids (set ids)]
      (is (= (count dfns) (count uids))
          (let [duplicates
                (keys
                 (filter
                  (comp #(> % 1) count second)
                  (group-by :id dfns)))]
            (str "Duplicate IDs: " (string/join ", " duplicates)))))))
