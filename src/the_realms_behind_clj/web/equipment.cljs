(ns the-realms-behind-clj.web.equipment 
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.web.db :as db]
            [the-realms-behind-clj.web.nav :refer [scroll-to]]
            [the-realms-behind-clj.web.text :refer [norm prompt-text
                                                    prompt-textarea]]
            [the-realms-behind-clj.web.utils :refer [e->value]]
            [the-realms-behind-clj.characters :as characters]))

(def WEAPONS "weapons")
(def ARMOR "armor")
(def SHIELDS "shield")
(def PACKS "packs")
(def CLOTHING "clothing")
(def ITEMS "items")

(def HOME "equipment-home")

(def -equipment (r/atom nil))
(def -enhancements (r/atom nil))

(do
  (.then (db/all-equipment)
         #(reset! -equipment %))
  (.then (db/all-enhancements)
         #(reset! -enhancements %)))

(def -workshop (r/atom nil))
(def -workshopping? (r/atom false))

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

(defn print-bulk [bulk]
  (if (number? bulk)
    bulk
    (norm bulk)))

(defn equipment-level [equipment]
  (->> (:enhancements equipment)
       (map :level)
       (reduce + (:level equipment 0))))

(defn reset-equipment! []
  (.then (db/all-equipment)
         #(reset! -equipment %)))

(defn print-equipment [equipment & extra]
  [:div.box>div.content
   [:p
    [:strong (:name equipment)]
    " "
    [:em "(level " (equipment-level equipment) ")"]]
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
           [:th (norm header)]))
        (when (seq (:enhancements equipment []))
          [:th "Enhancements"])
        (let [id (:id equipment)]
          (when (and (string? id)
                     (some? (re-matches #"^equipment/.+$" id)))
            [:th "Delete?"]))]]
      [:tbody
       [:tr
        (doall
         (for [header eq-headers
               :when (get equipment header)
               :let [value (get equipment header)]]
           ^{:key header}
           [:td (cond
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
                  :else value)]))
        (when (seq (:enhancements equipment []))
          [:td
           (->> (:enhancements equipment [])
                (map (comp norm :name))
                (string/join ", "))])
        (let [id (:id equipment)]
          (when (and (string? id)
                     (some? (re-matches #"^equipment/.+$" id)))
            [:td
             [:button.button.is-narrow.is-danger
              {:on-click
               #(when (.confirm js/window
                                (str "Are you sure you want to delete "
                                     (:name equipment)
                                     "?"))
                  (.then (db/remove-id! db/db id) reset-equipment!))}
              "Delete Equipment"]]))]]])
   extra])

(defn print-equipment-short [equipment & extra]
  [:div.box
   [:p
    [:strong (:name equipment)]
    " "
    [:em "(level " (equipment-level equipment) ")"]]
   [:p (:description equipment)]
   (let [eq-headers
         [:slot :bulk
          :accuracy :damage :defense
          :range :might :resists
          :inertia :stowage]]
     [:ul
      (doall
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
         [:li [:strong (norm header)] ": " pr-value]))
      (when-let [enhancements (seq (:enhancements equipment #{}))]
        [:li "Enhancements"
         [:ul
          (doall
           (for [enhancement enhancements]
             ^{:key (:id enhancement)}
             [:li (:name enhancement)]))]])])
   extra])

(defn new-equipment
  ([]
   [new-equipment -equipment -enhancements -workshopping? -workshop])
  ([-equipment -enhancements -workshopping? -workshop]
   (let [enhancements @-enhancements
         filter-by-tag (fn [tag] (filter #((:tags % #{}) tag) enhancements))
         defects (filter-by-tag :defect)
         masterworks (filter-by-tag :masterwork)
         enchantments (filter-by-tag :enchantment)
         curses (filter-by-tag :curse)]
     [:div.box
      [:form.form
       [:div.field
        [:label.label "Base Item"]
        [:div.control
         [:div.select
          [:select
           {:on-change
            #(reset! -workshop (-> % e->value resources/name->content))}
           [:<>
            [:option ""]
            (doall
             (for [equipment (sort-by :name  @-equipment)
                   :when (empty? (filter #{:uncraftable} (:tags equipment #{})))]
               ^{:key (:id equipment)}
               [:option (:name equipment)]))]]]]]
       (when @-workshop
         [:<>
          [:div.field
           [:label.label "Name"]
           [:div.control
            [prompt-text
             {:get-value #(:name @-workshop "")
              :on-change #(swap! -workshop assoc :name (e->value %))}]]]
          [:div.field
           [:label.label "Description"]
           [:div.control
            [prompt-textarea
             {:get-value #(string/replace (:description @-workshop "") #"\s{2,}" " ")
              :on-change #(swap! -workshop assoc :description (e->value %))}]]]
          (doall
           (for [[title group] [["Defects" defects]
                                ["Masterworks" masterworks]
                                ["Curses" curses]
                                ["Enchantments" enchantments]]
                 :let [sorted-group
                       (->> group
                            (sort-by :name)
                            (sort-by equipment-level)
                            (filter #((:tags % #{}) (:slot @-workshop))))]
                 :when (seq sorted-group)]
             ^{:key title}
             [:div.field
              [:label.label title]
              (doall
               (for [thing sorted-group]
                 ^{:key (:id thing)}
                 [:div.field
                  [:div.control
                   [:label.checkbox
                    [:input {:type "checkbox"
                             :on-change #(if ((:enhancements @-workshop #{}) thing)
                                           (swap! -workshop update :enhancements disj thing)
                                           (swap! -workshop update :enhancements conj thing))}]
                    " "
                    [:em (:name thing)]
                    " "
                    "(level " (:level thing) ")"
                    ": "
                    (:description thing)]]]))]))
          [print-equipment @-workshop]
          [:div.level
           [:div.level-item
            [:span "Steps: "
             (characters/xp-cost 3 (equipment-level @-workshop))]]
           [:div.level-item
            [:span "Materials: "
             (->> (:enhancements @-workshop)
                  (map :materials)
                  (reduce into (:materials @-workshop))
                  (map norm)
                  (sort)
                  (string/join ", "))]]]
          [:div.level
           [:div.level-item
            [:button.button.is-fullwidth.is-success
             {:on-click
              #(let [equipment @-workshop
                     uuid (db/equipment-uuid)
                     equipment* (assoc equipment :id uuid)]
                 (.then
                  (db/upsert-doc db/db uuid equipment*)
                  (fn [& _] (reset-equipment!)))
                 (reset! -workshop nil)
                 (reset! -workshopping? false))}
             "Save Equipment"]]]])]])))

(defn list-weapons [weapons]
  [:<>
   [:h4 {:name WEAPONS} "Weapons"]
   [:table.table.is-fullwidth
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
      (for [weapon weapons]
        ^{:key (:id weapon)}
        [:tr
         [:td (:name weapon)]
         [:td (equipment-level weapon)]
         [:td (:accuracy weapon)]
         [:td (:damage weapon)]
         [:td (:defense weapon)]
         [:td (print-range-expr (:range weapon))]
         [:td (print-bulk (:bulk weapon))]
         [:td (:might weapon)]]))]]
   (doall
    (for [weapon weapons]
      ^{:key (:id weapon)}
      [print-equipment weapon]))])

(defn list-shields [shields]
  [:<>
   [:h4 {:name SHIELDS} "Shields"]
   [:table.table.is-fullwidth
    [:thead
     [:tr
      [:th "Name"]
      [:th "Level"]
      [:th "Resists"]
      [:th "Damage"]
      [:th "Defense"]
      [:th "Inertia"]
      [:th "Bulk"]
      [:th "Might"]]]
    [:tbody
     (doall
      (for [shield shields]
        ^{:key (:id shield)}
        [:tr
         [:td (:name shield)]
         [:td (equipment-level shield)]
         [:td
          (string/join
           ", "
           (for [[group x] (:resists shield)]
             ^{:key group}
             (str (norm group)
                  " "
                  x)))]
         [:td (:damage shield)]
         [:td (:defense shield)]
         [:td (:inertia shield)]
         [:td (print-bulk (:bulk shield))]
         [:td (:might shield)]]))]]
   (doall
    (for [shield shields]
      ^{:key (:id shield)}
      [print-equipment shield]))])

(defn list-armor [armor]
  [:<>
   [:h4 {:name ARMOR} "Armor"]
   [:table.table.is-fullwidth
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
      (for [armor armor]
        ^{:key (:id armor)}
        [:tr
         [:td (:name armor)]
         [:td (equipment-level armor)]
         [:td
          (string/join
           ", "
           (for [[group x] (:resists armor)]
             ^{:key group}
             (str (norm group)
                  " "
                  x)))]
         [:td (:inertia armor)]
         [:td (print-bulk (:bulk armor))]
         [:td (:might armor)]]))]]
   (doall
    (for [armor armor]
      ^{:key (:id armor)}
      [print-equipment armor]))])

(defn list-packs [packs]
  [:<>
   [:h4 {:name PACKS} "Packs"]
   [:table.table.is-fullwidth
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
      (for [pack packs]
        ^{:key (:id pack)}
        [:tr
         [:td (:name pack)]
         [:td (equipment-level pack)]
         [:td (norm (:slot pack))]
         [:td (:stowage pack)]
         [:td (:might pack)]
         [:td (print-bulk (:bulk pack))]]))]]
   (doall
    (for [pack packs]
      ^{:key (:id pack)}
      [print-equipment pack]))])

(defn list-clothing [clothing]
  [:<>
   [:h4 {:name CLOTHING} "Clothing"]
   [:table.table.is-fullwidth
    [:thead
     [:tr
      [:th "Name"]
      [:th "Level"]
      [:th "Bulk"]
      [:th "Might"]]]
    [:tbody
     (doall
      (for [single-clothing clothing]
        ^{:key (:id single-clothing)}
        [:tr
         [:td (:name single-clothing)]
         [:td (equipment-level single-clothing)]
         [:td (print-bulk (:bulk single-clothing))]
         [:td (:might single-clothing)]]))]]
   (doall
    (for [single-clothing clothing]
      ^{:key (:id single-clothing)}
      [print-equipment single-clothing]))])

(defn list-items [items]
  [:<>
   [:h4 {:name ITEMS} "Items"]
   [:table.table
    [:thead
     [:tr
      [:th "Name"]
      [:th "Level"]
      [:th "Bulk"]]]
    [:tbody
     (doall
      (for [item items]
        ^{:key (:id item)}
        [:tr
         [:td (:name item)]
         [:td (equipment-level item)]
         [:td (print-bulk (:bulk item))]]))]]
   (doall
    (for [item items]
      ^{:key (:id item)}
      [print-equipment item]))])

(defn equipment-view
  [_]
  [:div.columns
   [:div.column.is-2
    [:div.box>div.content
     [:h1.title {:name HOME} "Equipment"]
     [:p "Content:"]
     [:ul
      [:li [:button.is-link
            {:on-click (scroll-to WEAPONS)}
            "Weapons"]]
      [:li [:button.is-link
            {:on-click (scroll-to SHIELDS)}
            "Shields"]]
      [:li [:button.is-link
            {:on-click (scroll-to ARMOR)}
            "Armor"]]
      [:li [:button.is-link
            {:on-click (scroll-to PACKS)}
            "Packs"]]
      [:li [:button.is-link
            {:on-click (scroll-to CLOTHING)}
            "Clothing"]]
      [:li [:button.is-link
            {:on-click (scroll-to ITEMS)}
            "Items"]]]]]
   [:div.column
    [:div.box>div.content
     (let [grouped (group-by :slot @-equipment)]
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
          [new-equipment])
        [list-weapons
         (->> (:weapon grouped [])
              (sort-by :name)
              (sort-by equipment-level)
              (sort-by :damage))]
        [list-shields
         (->> (:shield grouped [])
              (sort-by :name)
              (sort-by equipment-level)
              (sort-by :inertia))]
        [list-armor
         (->> (:armor grouped [])
              (sort-by :name)
              (sort-by :inertia)
              (sort-by equipment-level))]
        [list-packs
         (->> (flatten (vals (select-keys grouped [:pack :belt])))
              (sort-by :name)
              (sort-by equipment-level)
              (sort-by :slot))]
        [list-clothing
         (->> (flatten (vals (select-keys grouped [:head :torso :hands :feet :ring])))
              (sort-by :name)
              (sort-by equipment-level)
              (sort-by :slot))]
        [list-items
         (->> (:item grouped [])
              (sort-by :name)
              (sort-by equipment-level))]])]]])
