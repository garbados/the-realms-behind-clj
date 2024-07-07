(ns the-realms-behind-clj.web.equipment 
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [the-realms-behind-clj.web.db :as db]
            [the-realms-behind-clj.web.nav :refer [scroll-to]]
            [the-realms-behind-clj.web.text :refer [norm prompt-text
                                                    prompt-textarea]]))

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
  [:div.box>div.content
   [:p
    [:strong (:name equipment)]
    " "
    [:em "(level " (:level equipment) ")"]]
   (when-let [description (:description equipment)]
     [:p description])
   (let [eq-headers
         [:slot
          :damage :resists
          :accuracy :defense :range
          :inertia :stowage
          :might :bulk]]
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

(defn new-equipment
  ([-equipment -enhancements]
   (let [-workshop (r/atom {})]
     [new-equipment -equipment -enhancements -workshop
      (r/atom "") (r/atom "")]))
  ([-equipment -enhancements -workshop -name -description]
   (let [enhancements @-enhancements
         filter-by-tag (fn [tag] (filter #((:tags % #{}) tag) enhancements))
         defects (filter-by-tag :defect)
         masterworks (filter-by-tag :masterwork)
         enchantments (filter-by-tag :enchantment)
         curses (filter-by-tag :curse)]
     [:div.box
      [:form.form
       [:div.field
        [:label.label "Name"]
        [:div.control
         [prompt-text -name]]]
       [:div.field
        [:label.label "Description"]
        [:div.control
         [prompt-textarea -description]]]]])))

(defn equipment-view
  ([_]
   (let [-equipment (r/atom [])
         -enhancements (r/atom [])
         -workshopping? (r/atom false)]
     (.then (db/all-equipment)
            #(reset! -equipment %))
     (.then (db/all-enhancements)
            #(reset! -enhancements %))
     [equipment-view _ -equipment -enhancements -workshopping?]))
  ([_ -equipment -enhancements -workshopping?]
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
      (let [grouped (group-by :slot @-equipment)
            sorted-weapons (->> (:weapon grouped [])
                                (sort-by :damage)
                                (sort-by :name)
                                (sort-by :level))
            sorted-armor (->> (:armor grouped [])
                              (sort-by :name)
                              (sort-by :level)
                              (sort-by :inertia))
            sorted-packs (->> (flatten (vals (select-keys grouped [:pack :belt])))
                              (sort-by :name)
                              (sort-by :level)
                              (sort-by :slot))
            sorted-items (->> (:item grouped [])
                              (sort-by :name)
                              (sort-by :level))]
        [:<>
         [:p
          (if @-workshopping?
            [:button.button.is-fullwidth.is-dark
             {:on-click #(reset! -workshopping? false)}
             "Close Workshop"]
            [:button.button.is-fullwidth.is-light
             {:on-click #(reset! -workshopping? true)}
             "Open Workshop"])]
         (when @-workshopping?
           [new-equipment -equipment -enhancements])
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
               [:td (print-range-expr (:range weapon))]
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
           [print-equipment armor])
         [:h4 {:name PACKS} "Packs"]
         [:table.table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Level"]
            [:th "Slot"]
            [:th "Stowage"]
            [:th "Might"]
            [:th "Bulk"]]]
          [:tbody
           (doall
            (for [pack sorted-packs]
              ^{:key (:id pack)}
              [:tr
               [:td (:name pack)]
               [:td (:level pack)]
               [:td (norm (:slot pack))]
               [:td (:stowage pack)]
               [:td (:might pack)]
               [:td (:bulk pack)]]))]]
         (for [pack sorted-packs]
           ^{:key (:id pack)}
           [print-equipment pack])
         [:h4 {:name ITEMS} "Items"]
         [:table.table
          [:thead
           [:tr
            [:th "Name"]
            [:th "Level"]
            [:th "Bulk"]]]
          [:tbody
           (doall
            (for [item sorted-items]
              ^{:key (:id item)}
              [:tr
               [:td (:name item)]
               [:td (:level item)]
               [:td (:bulk item)]]))]]
         (for [item sorted-items]
           ^{:key (:id item)}
           [print-equipment item])])]]]))

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
