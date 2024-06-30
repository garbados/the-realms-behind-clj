(ns the-realms-behind-clj.web.equipment 
  (:require [clojure.string :as string]
            [the-realms-behind-clj.web.nav :refer [scroll-to]]
            [the-realms-behind-clj.web.text :refer [norm]]))

(def WEAPONS "weapons")
(def ARMOR "armor")
(def PACKS "packs")
(def ITEMS "items")

(defn print-equipment [equipment]
  [:div.box
   [:p [:strong (:name equipment)]]
   [:p (:description equipment)]
   [:ul
    (for [key [:slot :bulk
               :accuracy :damage :defense
               :range :might
               :inertia :stowage]]
      (when-let [value (get equipment key)]
        [:li (norm key) ": " value]))
    (when-let [resists (get equipment :resists)]
      [:li "Resists: "
       (string/join
        " ; "
        (for [[element x] resists]
          (str (norm element) " " x)))])]])

(defn equipment-view [some-equipment]
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
             [:td (:might weapon)]])]]
        (for [weapon sorted-weapons]
          ^{:key (:id weapon)}
          [:div.box
           [:h5 (:name weapon)]
           [:p (:description weapon)]])
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
             [:td (:might armor)]])]]
        (for [armor sorted-armor]
          ^{:key (:id armor)}
          [:div.box
           [:h5 (:name armor)]
           [:p (:description armor)]])])]]])
