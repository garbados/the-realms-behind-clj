(ns the-realms-behind-clj.web.characters 
  (:require [reagent.core :as r]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.specs :as specs]
            [the-realms-behind-clj.web.equipment :refer [print-equipment]]
            [the-realms-behind-clj.web.feats :refer [print-feat]]
            [the-realms-behind-clj.web.text :refer [norm prompt-text
                                                    prompt-textarea]]))

(def active-character (r/atom nil))
(def editing-character? (r/atom false))

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
       [:p
        [:figure.image.is-128x128
         [:img {:src image-url}]]])]]
   [:div.columns
    [:div.column.is-3>div.content
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
          [:td [:em (norm attr) " + " base-skill]]])]]]
    [:div.column.is-3>div.content
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
             [:td [:em (norm skill)]]])]]])]
    (let [{:keys [equipped at-hand inventory]} character]
      [:div.column.is-3>div.content
       [:h5 "Equipped (" (reduce + (map :bulk equipped)) ")"]
       (for [equipment equipped]
         ^{:key (:id equipment)}
         [print-equipment equipment])
       [:h5 "At-Hand ("  (reduce + (map :bulk at-hand)) ")"]
       (for [equipment at-hand]
         ^{:key (:id equipment)}
         [print-equipment equipment])
       [:h5 "Inventory (" (reduce + (map :bulk inventory)) ")"]
       (for [equipment inventory]
         ^{:key (:id equipment)}
         [print-equipment equipment])])
    [:div.column.is-3>div.content
     [:h5 "Feats"]
     (for [feat (sort (:feats character))
           :let [details (resources/resolve-link feat)
                 trimmed (select-keys details [:name :description])]]
       ^{:key feat}
       [print-feat trimmed])]]])

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
     [:p.subtitle "XP Free: " (:experience @-character 0)]
     [:div.columns
      [:div.column.is-6
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
              "Give (+" cost+1 ")"]]])]]

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
              "Give (+" cost+1 ")"]]])]]]]]))

(defn characters-view [characters]
  [:div.columns
   [:div.column.is-2
    [:div.box>div.content
     [:p "Characters:"]
     (for [character characters]
       ^{:key character}
       [:button.button.is-fullwidth
        {:on-click #(reset! active-character character)
         :class (when (= @active-character character) "is-primary")}
        (get-in character [:bio :name])
        (when (nil? (:stats character))
          " [sample]")])
     [:hr]
     [:button.button.is-fullwidth.is-success
      {:on-click
       #(do
          (reset! active-character {})
          (reset! editing-character? true))}
      "New Character"]]]
   [:div.column
    (when-let [character @active-character]
      [character-sheet character])
    (let [-character (r/atom @active-character)]
      [edit-character -character])]])