(ns the-realms-behind-clj.web.text 
  (:require [clojure.string :as string]))

(defn merge-attrs [& attrs]
  (merge-with merge attrs))

(def center-text {:style {:text-align "center"}})

(defn norm [s]
  (cond
    (= :ap s) "AP"
    :else
    (as-> (name s) $
      (string/split $ #"-")
      (map string/capitalize $)
      (string/join " " $))))

(defn prompt-text [value & [on-submit]]
  [:input.input
   (cond->
    {:type "text"
     :value @value
     :on-change #(reset! value (-> % .-target .-value))}
     on-submit (assoc :on-key-down
                      (fn [e]
                        (when (= 13 (.-which e))
                          (on-submit @value)))))])

(defn prompt-textarea [value]
  [:textarea.textarea
   {:on-change #(reset! value (-> % .-target .-value))
    :rows 3
    :value @value}])
