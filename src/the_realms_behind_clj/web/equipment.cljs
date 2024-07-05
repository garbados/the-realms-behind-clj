(ns the-realms-behind-clj.web.equipment 
  (:require [clojure.string :as string]
            [the-realms-behind-clj.web.nav :refer [scroll-to]]
            [the-realms-behind-clj.web.text :refer [norm]]
            [the-realms-behind-clj.web.db :as db]))

(def WEAPONS "weapons")
(def ARMOR "armor")
(def PACKS "packs")
(def ITEMS "items")

(defn print-range-expr [range-expr]
  (cond
    (number? range-expr) (str range-expr)
    (= :close range-expr) "Close"
    (map? range-expr)
    (->> (seq range-expr)
         (map (fn [[attr x]] (str (norm attr) " x" x)))
         (string/join "; "))
    (seq range-expr)
    (string/join
     " or "
     (map print-range-expr range-expr))
    :else range-expr))

(defn print-equipment [equipment & extra]
  [:div.box
   [:p
    [:strong (:name equipment)]
    " "
    [:em "(level " (:level equipment) ")"]]
   [:p (:description equipment)]
   (let [eq-headers
         [:slot :bulk
          :accuracy :damage :defense
          :range :might :resists
          :inertia :stowage]]
     [:table.table.is-fullwidth
      [:thead
       [:tr
        (doall
         (for [header eq-headers
               :when (get equipment header)]
           ^{:key header}
           [:th (norm header)]))]]
      [:tbody
       [:tr
        (doall
         (for [header eq-headers
               :when (get equipment header)
               :let [value (get equipment header)]]
           ^{:key header}
           [:th (cond
                  (= :range header)
                  (print-range-expr value)
                  (= :resists header)
                  (->> (seq value)
                       (map
                        (fn [[elem-name x]]
                          (str (norm elem-name) " " x)))
                       (string/join "; "))
                  (keyword? value)
                  (norm value)
                  :else value)]))]]])
   extra])

(defn equipment-view
  ([_ ]
   [equipment-view _ (db/all-equipment)])
  ([_ some-equipment]
   [:div.columns
    [:div.column.is-2
     [:div.box>div.content
      [:h1.title "Equipment"]
      [:p "Content:"]
      [:ul
       [:li [:button.is-link
             {:on-click (scroll-to WEAPONS)}
             "Weapons"]]
       [:li [:button.is-link
             {:on-click (scroll-to ARMOR)}
             "Armor"]]
       [:li [:button.is-link
             {:on-click (scroll-to PACKS)}
             "Packs"]]
       [:li [:button.is-link
             {:on-click (scroll-to ITEMS)}
             "Items"]]]]]
    [:div.column
     [:div.box>div.content
      (let [grouped (group-by :slot some-equipment)
            sorted-weapons (->> grouped :weapon (sort-by :name))
            sorted-armor (->> grouped :armor (sort-by :inertia))]
        [:<>
         [:h4 {:name WEAPONS} "Weapons"]
         [:table.table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Level"]
            [:th "Accuracy"]
            [:th "Damage"]
            [:th "Defense"]
            [:th "Range"]
            [:th "Bulk"]
            [:th "Might"]]]
          [:tbody
           (doall
            (for [weapon sorted-weapons]
              ^{:key (:id weapon)}
              [:tr
               [:td (:name weapon)]
               [:td (:level weapon)]
               [:td (:accuracy weapon)]
               [:td (:damage weapon)]
               [:td (:defense weapon)]
               [:td (:range weapon)]
               [:td (:bulk weapon)]
               [:td (:might weapon)]]))]]
         (doall
          (for [weapon sorted-weapons]
            ^{:key (:id weapon)}
            [print-equipment weapon]))
         [:h4 {:name ARMOR} "Armor"]
         [:table.table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Level"]
            [:th "Resists"]
            [:th "Inertia"]
            [:th "Bulk"]
            [:th "Might"]]]
          [:tbody
           (doall
            (for [armor sorted-armor]
              ^{:key (:id armor)}
              [:tr
               [:td (:name armor)]
               [:td (:level armor)]
               [:td
                (string/join
                 ", "
                 (for [[group x] (:resists armor)]
                   ^{:key group}
                   (str (norm group)
                        " "
                        x)))]
               [:td (:inertia armor)]
               [:td (:bulk armor)]
               [:td (:might armor)]]))]]
         (for [armor sorted-armor]
           ^{:key (:id armor)}
           [print-equipment armor])])]]]))

(defn print-equipment-short [equipment & extra]
  [:div.box
   [:p
    [:strong (:name equipment)]
    " "
    [:em "(level " (:level equipment) ")"]]
   [:p (:description equipment)]
   (let [eq-headers
         [:slot :bulk
          :accuracy :damage :defense
          :range :might :resists
          :inertia :stowage]]
     [:ul
      (for [header eq-headers
            :let [value (get equipment header)
                  pr-value
                  (cond
                    (keyword? value)
                    (norm value)
                    (map? value)
                    (string/join
                     ", "
                     (map
                      (fn [[k v]]
                        (str (norm k) " " v))
                      value))
                    :else
                    value)]
            :when (some? value)]
        ^{:key header}
        [:li [:strong (norm header)] ": " pr-value])])
   extra])
