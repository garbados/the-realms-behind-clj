(ns the-realms-behind-clj.web.nav 
  (:require [clojure.string :as string]))

(defn section-name [& sections]
  (string/join "-" sections))

(defn scroll-to [elem-name]
  (fn []
    (.scrollIntoView
     (first
      (.getElementsByName js/document elem-name)))))
