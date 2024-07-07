(ns the-realms-behind-clj.resources-test
  (:require [clojure.spec.alpha :as s]
            [clojure.string :as string]
            [clojure.test :refer [deftest is testing]]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.specs :as specs]))

(deftest valid-resources
  (testing "Feats resources contain valid definitions."
    (doseq [feat (resources/feats)]
      (is (s/valid? ::specs/feat feat)
          (s/explain-str ::specs/feat feat))))
  (testing "Equipment resources contain valid definitions."
    (doseq [single-equipment (resources/equipment)]
      (is (s/valid? ::specs/single-equipment single-equipment)
          (s/explain-str ::specs/single-equipment single-equipment))))
  (testing "Enhancement resources contain valid definitions."
    (doseq [enhancement (resources/enhancements)]
      (is (s/valid? ::specs/enhancement enhancement)
          (s/explain-str ::specs/enhancement enhancement))))
  (testing "Features resources contain valid definitions."
    (doseq [feature (resources/features)]
      (is (s/valid? ::specs/feature feature)
          (s/explain-str ::specs/feature feature))))
  (testing "Unique IDs across content"
    (let [all-content resources/all-content
          ids (map :id all-content)
          uids (set ids)]
      (is (= (count all-content) (count uids))
          (let [duplicates
                (keys
                 (filter
                  (comp #(> % 1) count second)
                  (group-by :id all-content)))]
            (str "Duplicate IDs: " (string/join ", " duplicates))))))
  (testing "Unique names across content"
    (let [all-content resources/all-content
          names (map :name (filter (comp some? :name) all-content))
          unique-names (set names)]
      (is (= (count names) (count unique-names))
          (let [duplicates
                (first
                 (reduce
                  (fn [[all names] cname]
                    (if (names cname)
                      [all (disj names cname)]
                      [(conj all cname) names]))
                  [[] unique-names]
                  names))]
            (str "IDs with duplicate names: " (string/join ", " duplicates)))))))
