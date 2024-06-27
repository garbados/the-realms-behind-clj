(ns the-realms-behind-clj.feats-test
  (:require [clojure.test :refer [deftest]]
            [the-realms-behind-clj.feats :as feats]
            [the-realms-behind-clj.test-utils :refer [spec-test-syms]]))

(deftest feats-spec-tests
  (spec-test-syms
   [`feats/feats+tag=>feats
    `feats/feats-tag=>feats]))
