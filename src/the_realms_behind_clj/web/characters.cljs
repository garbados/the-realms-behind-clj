(ns the-realms-behind-clj.web.characters 
  (:require [reagent.core :as r]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.specs :as specs]
            [the-realms-behind-clj.web.db :as db]
            [the-realms-behind-clj.web.equipment :refer [print-equipment]]
            [the-realms-behind-clj.web.feats :refer [print-feat]]
            [the-realms-behind-clj.web.text :refer [norm prompt-text
                                                    prompt-textarea]]))

(def overflow-area
  {:style {:overflow "scroll"
           :max-height 700}})

(defn- print-attributes [character]
  [:div.content
   [:h5 "Attributes:"]
   [:table.table
    [:thead
     [:tr
      [:th "Body"]
      [:th "Mind"]
      [:th "Spirit"]
      [:th "Luck"]]]
    [:tbody
     [:tr
      (for [attr [:body :mind :spirit :luck]]
        ^{:key attr}
        [:td (get-in character [:attributes attr])])]]]
   [:h5 "Skills:"]
   [:table.table
    [:thead
     [:tr
      [:th "X"]
      [:th "Skill"]
      [:th "Formula"]]]
    [:tbody
     (for [skill (sort specs/skills)
           :let [skill-name (norm skill)
                 base-skill (get-in character [:skills skill] 0)
                 attr (characters/skill->attribute skill)
                 x (characters/character-skill character skill)]]
       ^{:key skill}
       [:tr
        [:td x]
        [:td skill-name]
        [:td [:em (norm attr) " + " base-skill]]])]]])

(defn- print-stats [character]
  [:div.content
   [:h5 "Stats"]
   (let [stats (:stats character (characters/base-stats character))]
     [:<>
      [:ul
       (let [{:keys [health max-health]} stats
             [shallow deep] health
             [shallow* deep*] max-health]
         [:li "Health: " shallow " / " shallow* " || " deep " / " deep*])
       (for [[stat x] (select-keys stats [:will :fortune :draw :speed :initiative])]
         ^{:key stat}
         [:li (norm stat) ": " x])
       (let [carrying-bulk (characters/carrying-bulk character)
             carry-limit (:carrying-capacity stats)]
         [:li "Carrying: " carrying-bulk " / " carry-limit])]
      [:h5 "Defenses"]
      [:table.table
       [:thead
        [:tr
         [:th "X"]
         [:th "Defense"]
         [:th "Skill"]]]
       [:tbody
        (for [[defense x] (:defenses stats)
              :let [defense-name (norm defense)
                    skill (characters/defense->skill defense)]]
          ^{:key defense}
          [:tr
           [:td x]
           [:td defense-name]
           [:td [:em (norm skill)]]])]]])])

(defn- character-sheet [character]
  [:div.box
   [:div.columns
    [:div.column
     [:div.content
      [:h4.title (get-in character [:bio :name])
       [:strong
        " (" (characters/base-xp character) "xp; "
        (:experience character) " free)"]]
      [:p [:em "Played by " (get-in character [:bio :player])]]
      [:p (get-in character [:bio :description])]]]
    [:div.column.is-narrow
     (when-let [image-url (get-in character [:bio :image-url])]
       [:figure.image.is-128x128
        [:img {:src image-url}]])]]
   [:div.columns
    [:div.column.is-3
     [print-attributes character]]
    [:div.column.is-3
     [print-stats character]]
    (let [{:keys [equipped at-hand inventory]} character]
      [:div.column.is-3>div.content
       [:h5 "Equipped (" (reduce + 0 (map :bulk equipped)) ")"]
       (for [equipment equipped]
         ^{:key (:id equipment)}
         [print-equipment equipment])
       [:h5 "At-Hand ("  (reduce + 0 (map :bulk at-hand)) ")"]
       (for [equipment at-hand]
         ^{:key (:id equipment)}
         [print-equipment equipment])
       [:h5 "Inventory (" (reduce + 0 (map :bulk inventory)) ")"]
       (for [equipment inventory]
         ^{:key (:id equipment)}
         [print-equipment equipment])])
    [:div.column.is-3>div.content
     [:h5 "Feats"]
     (for [feat (sort-by :name (:feats character))
           :let [trimmed (select-keys feat [:name :description])]]
       ^{:key (:id feat)}
       [print-feat trimmed])]]])

(defn- edit-attributes [-character]
  [:<>
   [:div.content>h3 "Attributes"]
   [:table.table.is-fullwidth
    [:thead
     [:tr
      [:th "Name"]
      [:th "Level"]
      [:th "Take"]
      [:th "Sell"]
      [:th "Buy"]
      [:th "Give"]]]
    [:tbody
     (doall
      (for [attr [:body :mind :spirit :luck]
            :let [x (get-in @-character [:attributes attr] 0)
                  cost (* x 2)
                  cost+1 (+ cost 2)]]
        ^{:key attr}
        [:tr
         [:td (norm attr)]
         [:td x]
         [:td
          [:button.button.is-fullwidth.is-danger
           {:disabled (not (pos-int? x))
            :on-click
            #(swap! -character update-in [:attributes attr] dec)}
           "Take (-" cost ")"]]
         [:td
          [:button.button.is-fullwidth.is-warning
           {:disabled (not (pos-int? x))
            :on-click
            #(do
               (swap! -character update-in [:attributes attr] dec)
               (swap! -character update :experience + cost))}
           "Sell"]]
         [:td
          [:button.button.is-fullwidth.is-success
           {:disabled (< (:experience @-character 0) cost+1)
            :on-click
            #(do
               (swap! -character update-in [:attributes attr] inc)
               (swap! -character update :experience - cost+1))}
           "Buy"]]
         [:td
          [:button.button.is-fullwidth.is-info
           {:on-click
            #(swap! -character update-in [:attributes attr] inc)}
           "Give (+" cost+1 ")"]]]))]]])

(defn- edit-skills [-character]
  [:<>
   [:div.content>h3 "Skills"]
   [:table.table.is-fullwidth
    [:thead
     [:tr
      [:th "Name"]
      [:th "Level"]
      [:th "Take"]
      [:th "Sell"]
      [:th "Buy"]
      [:th "Give"]]]
    [:tbody
     (doall
      (for [skill (sort specs/skills)
            :let [x (get-in @-character [:skills skill] 0)
                  attr (characters/skill->attribute skill)
                  x* (+ (get-in @-character [:attributes attr] 0)
                        x)
                  cost x
                  cost+1 (inc x)]]
        ^{:key skill}
        [:tr
         [:td (norm skill) " " [:strong "(" (norm attr) ")"]]
         [:td x " " [:strong "(" x* ")"]]
         [:td
          [:button.button.is-fullwidth.is-danger
           {:disabled (not (pos-int? x))
            :on-click
            #(swap! -character update-in [:skills skill] dec)}
           "Take (-" cost ")"]]
         [:td
          [:button.button.is-fullwidth.is-warning
           {:disabled (not (pos-int? x))
            :on-click
            #(do
               (swap! -character update-in [:skills skill] dec)
               (swap! -character update :experience + cost))}
           "Sell"]]
         [:td
          [:button.button.is-fullwidth.is-success
           {:disabled (< (:experience @-character 0) cost+1)
            :on-click
            #(do
               (swap! -character update-in [:skills skill] inc)
               (swap! -character update :experience - cost+1))}
           "Buy"]]
         [:td
          [:button.button.is-fullwidth.is-info
           {:on-click
            #(swap! -character update-in [:skills skill] inc)}
           "Give (+" cost+1 ")"]]]))]]])

(defn- edit-feats [-character]
  [:div.columns
   [:div.column.is-6
    [:div.content
     [:h3 "Known Feats"]
     [:div
      overflow-area
      (for [feat (:feats @-character #{})
            :let [x (:level feat)
                  cost (* x 3)]]
        ^{:key (:id feat)}
        [print-feat feat
         [:div.level
          [:div.level-item
           [:button.button.is-fullwidth.is-danger
            {:on-click
             #(swap! -character update :feats disj feat)}
            "Take Feat (-" cost ")"]]
          [:div.level-item
           [:button.button.is-fullwidth.is-warning
            {:on-click
             #(do
                (swap! -character update :experience + cost)
                (swap! -character update :feats disj feat))}
            "Sell Feat"]]]])]]]
   [:div.column.is-6
    [:div.content
     [:h3 "Available Feats"]
     [:div
      overflow-area
      (for [feat (db/all-feats)
            :when
            (and (characters/can-use? @-character feat)
                 (empty? (filter #(= feat %) (:feats @-character))))
            :let [x (:level feat)
                  cost (* x 3)]]
        ^{:key (:id feat)}
        [print-feat feat
         [:div.level
          [:div.level-item
           [:button.button.is-fullwidth.is-success
            {:disabled
             (< (:experience @-character)
                cost)
             :on-click
             #(do
                (swap! -character update :experience - cost)
                (swap! -character update :feats conj feat))}
            "Buy"]]
          [:div.level-item
           [:button.button.is-fullwidth.is-info
            {:on-click
             #(swap! -character update :feats conj feat)}
            "Gain (+" cost ")"]]]])]]]])

(defn- edit-equipment [-character]
  [:div.columns
   [:div.column.is-6
    [:div.content
     [:h3 "Owned equipment"]
     [:div
      overflow-area
      (for [single-equipment
            (sort
             characters/sort-equipment
             (characters/carried-equipment @-character))]
        [print-equipment single-equipment
         (let [cost (characters/wealth-cost
                     (:level single-equipment))]
           [:div.level
            [:div.level-item
             [:button.button.is-fullwidth.is-danger
              #_{:on-click
                 #(swap! -character update :feats disj feat)}
              "Take (-" cost ")"]]
            [:div.level-item
             [:button.button.is-fullwidth.is-warning
              #_{:on-click
                 #(do
                    (swap! -character update :experience + cost)
                    (swap! -character update :feats conj feat))}
              "Sell"]]])])]]]
   [:div.column.is-6
    [:div.content
     [:h3 "Available equipment"]
     [:div
      overflow-area
      (for [single-equipment
            (sort
             characters/sort-equipment
             (db/all-equipment))]
        ^{:key (:id single-equipment)}
        [print-equipment single-equipment
         (let [cost (characters/wealth-cost
                     (:level single-equipment))]
           [:div.level
            [:div.level-item
             [:button.button.is-fullwidth.is-success
              #_{:on-click
                 #(swap! -character update :feats disj feat)}
              "Buy"]]
            [:div.level-item
             [:button.button.is-fullwidth.is-info
              #_{:on-click
                 #(do
                    (swap! -character update :experience + cost)
                    (swap! -character update :feats conj feat))}
              "Give (+" cost ")"]]])])]]]])

(defn- edit-character [-character]
  (let [-name (r/atom (get-in @-character [:bio :name] ""))
        -description (r/atom (get-in @-character [:bio :description] ""))
        -image-url (r/atom (get-in @-character [:bio :image-url] ""))]
    [:div.box
     [:div.content>h1 "New Character"]
     [:form.form
      [:div.field
       [:label.label "Name"]
       [:div.control
        [prompt-text -name]]
       [:p.help "The moniker other players will know you by."]]
      [:div.field
       [:label.label "Description"]
       [:div.control
        [prompt-textarea -description]]
       [:p.help "Use markdown!"]]
      [:div.field
       [:label.label "Image URL"]
       [:div.control
        [prompt-text -image-url]]
       [:p.help "The URL for your character's profile picture."]]]
     [:hr]
     [:h1.title "XP Total: " (characters/base-xp @-character)]
     [:p.subtitle
      "XP Free: "
      [:span
       [:input
        {:size 4
         :type "number"
         :on-change
         #(swap! -character assoc :experience (-> % .-target .-value))
         :value (:experience @-character 75)}]]]
     [:div.columns
      [:div.column.is-6
       [edit-attributes -character]
       [print-stats @-character]]
      [:div.column.is-6
       [edit-skills -character]]]
     [:hr]
     [edit-feats -character]
     [:hr]
     [:h1.title "Wealth: "
      (->> (characters/carried-equipment @-character)
           (map (comp characters/wealth-cost :level))
           (reduce + 0))]
     [:p.subtitle "Gold: "
      [:span
       [:input
        {:size 4
         :type "number"
         :on-change
         #(swap! -character assoc :gold (-> % .-target .-value))
         :value (:gold @-character 10)}]]]
     [edit-equipment -character]]))

(defn characters-view
  ([_]
   [characters-view
    (vals (db/all-characters))
    (r/atom characters/base-character)
    (r/atom true)])
  ([characters -character -editing?]
   [:div.columns
    [:div.column.is-2
     [:div.box>div.content
      [:p "Characters:"]
      (for [character characters]
        ^{:key (:id character)}
        [:button.button.is-fullwidth
         {:on-click #(do (reset! -character character)
                         (reset! -editing? false))
          :class (when (= @-character character) "is-primary")}
         (get-in character [:bio :name])
         (when (nil? (:stats character))
           " [sample]")])
      [:hr]
      [:button.button.is-fullwidth.is-success
       {:on-click
        #(do
           (reset! -character characters/base-character)
           (reset! -editing? true))}
       "New Character"]]]
    [:div.column
     (when-let [character (and (not @-editing?) @-character)]
       [character-sheet character])
     [edit-character -character]]]))
