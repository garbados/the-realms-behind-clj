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

(defn prompt-text [{:keys [on-submit on-blur on-change get-value]}]
  [:input.input
   (cond->
    {:type "text"
     :value (get-value)
     :on-change on-change}
     on-blur (assoc :on-blur on-blur)
     on-submit (assoc :on-key-down
                      (fn [e]
                        (when (= 13 (.-which e))
                          (on-submit get-value)))))])

(defn prompt-text-value [value & opts]
  [prompt-text
   (merge (or opts {})
          {:get-value (fn [] @value)
           :on-change #(reset! value (-> % .-target .-value))})])

(defn prompt-textarea [{:keys [on-blur on-change get-value]}]
  [:textarea.textarea
   (cond->
    {:rows 3
     :value (get-value)}
     on-blur (assoc :on-blur on-blur)
     on-change (assoc :on-change on-change))])

(defn prompt-textarea-value [value & opts]
  [prompt-textarea
   (merge {:get-value (fn [] @value)
           :on-change #(reset! value (-> % .-target .-value))}
          (or opts {}))])
