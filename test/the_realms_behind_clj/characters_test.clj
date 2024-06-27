(ns the-realms-behind-clj.characters-test
  (:require [clojure.test :refer [deftest testing is]]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.specs :as specs]
            [the-realms-behind-clj.test-utils :refer [spec-test-syms]]
            [clojure.spec.alpha :as s]))

(deftest validate-constants
  (testing "attribute->suit"
    (let [spec (s/map-of ::specs/attribute ::specs/suit)]
      (is (s/valid? spec characters/attribute->suit)
          (s/explain-str spec characters/attribute->suit))
      (is (empty? (remove specs/attributes (keys characters/attribute->suit)))
          "Errant attributes in map!")
      (is (empty? (remove specs/suits (vals characters/attribute->suit)))
          "Errant suits in map!")))
  (testing "skill->attribute"
    (let [spec (s/map-of ::specs/skill ::specs/attribute)]
      (is (s/valid? spec characters/skill->attribute)
          (s/explain-str spec characters/skill->attribute))
      (is (empty? (remove specs/skills (keys characters/skill->attribute)))
          "Errant skills in map!")
      (is (empty? (remove specs/attributes (vals characters/skill->attribute)))
          "Errant attributes in map!"))))

(deftest validate-sample-characters
  (doseq [character (vals characters/sample-characters)]
    (testing (str "Validate: " (:name character))
      (is (s/valid? ::specs/character character)
          (s/explain-str ::specs/character character)))))

(deftest spec-tests
  (spec-test-syms
   [`characters/character-skill
    `characters/character-defense
    `characters/base-stats
    `characters/base-xp
    `characters/carrying
    `characters/carrying-bulk]))
