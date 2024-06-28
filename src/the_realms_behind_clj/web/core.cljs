(ns the-realms-behind-clj.web.core
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [reagent.dom :as rd]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.feats :as feats]
            [the-realms-behind-clj.resources :as resources]
            [the-realms-behind-clj.specs :as specs]))

(def source-url "https://github.com/garbados/the-realms-behind-clj/")
(def intro-url (str source-url "blob/main/doc/intro.md"))

(def current-view (r/atom nil))

(def base-feats (resources/feats))
(def base-equipment (resources/equipment))
(def custom-feats (r/atom []))
(def custom-equipment (r/atom []))
(def custom-characters (r/atom {}))
(def active-character (r/atom nil))
(def editing-character? (r/atom false))

(defn all-characters []
  (merge characters/sample-characters
         @custom-characters))

(defn norm [s]
  (as-> (name s) $
    (string/split $ #"-")
    (map string/capitalize $)
    (string/join " " $)))

(defn scroll-to [elem-name]
  (fn []
    (.scrollIntoView
     (first
      (js/document.getElementsByName elem-name)))))

(defn merge-attrs [& attrs]
  (merge-with merge attrs))

(def center-text {:style {:text-align "center"}})

(defn section-name [& sections]
  (string/join "-" sections))

(defn- navbar []
  [:div.level
   [:div.level-left
    [:div.level-item
     [:h1.title "The Realms Behind"]]
    [:div.level-item
     [:a.button.is-info.is-light
      {:href (rfe/href ::index)}
      "Welcome"]]
    [:div.level-item
     [:a.button.is-primary.is-light
      {:href intro-url
       :target "_blank"}
      "Introduction"]]
    [:div.level-item
     [:a.button.is-info
      {:href (rfe/href ::feats)}
      "Feats"]]
    [:div.level-item
     [:a.button.is-info
      {:href (rfe/href ::equipment)}
      "Equipment"]]
    [:div.level-item
     [:a.button.is-primary
      {:href (rfe/href ::characters)}
      "Characters"]]
    [:div.level-item
     [:a.button.is-primary
      {:href (rfe/href ::npc-manager)}
      "NPC Manager"]]]
   [:div.level-right
    [:div.level-item
     [:p.subtitle
      [:strong "Made with love by DFB"]]]
    [:div.level-item
     [:a.button.is-info.is-light
      {:href source-url
       :target "_blank"}
      "Source 👩‍💻"]]]])

(defn- index-view []
  [:div.box>div.content
   [:h3 "You had never dreamed so furiously. So... shimmering emerald."]
   [:p "You stepped from nowhere into the Fae Queen's Court, where an endless foyer sprawled. White marble columns adorned with ivy trellises, and arching dome ceilings pocked with stained glass went on and on. Peering through different skylights revealed very, very different skies. Masked nobility moved in eerie unity to a tune you could only hear as if recalling it. Their formal dress and proper fits delayed your realization that there were no faces behind their masks."]
   [:p "Then She found you. Her eminence, Her elegance, Her rapturous, indescribable visage. She offered a green-gloved hand, and said without moving Her mouth, \"Dance with me.\" You took Her grasp before you could so much as consider it. She whirled you into Her arms and back out into Her excited tempo, back and forth, back and forth, for hours, days, weeks, months, years and years and years and years. Your feet grew so weary, but then they were so far away you could not remember having them; your hand clammed from Her touch, but then your hand was made of wood; your lips trembled at Her kiss, and then your lips parted no more, no matter how much you screamed."]
   [:p "It was your first Dancing Dream. You awakened to the same world as you fell asleep to, but something had come between you, as though your home reality lay on the other side of a thin, almost imperceptible veil. You snapped your fingers, just to prove you still had any, " [:em "and sparks burst from their tips!"]]
   [:p "First you asked yourself in alarm, \"What did She do to me?\" But the question faded, as though it had been sucked down into bog-muck. Another query took its place, wriggling to the front of your thoughts like a worm protruding from an apple:"]
   [:p [:em [:strong "What shall you do with My blessing?"]]]
   [:hr]
   [:h3 "Welcome, mortal..."]
   [:p [:em "The Realms Behind "]
    "is a tabletop roleplaying game
     and this is its website."]
   [:p "To learn more about the rules, see the "
    [:a {:href intro-url} "introduction."]]
   [:p "To get started making a character, "
    [:a {:href (rfe/href ::characters)} "click here!"]]])

(defn- print-feat [feat]
  [:div.box
   [:p [:strong (:name feat)]]
   [:p (:description feat)]
   (when-let [requirements (:requirements feat)]
     [:<>
      [:p [:em "Requires: "]]
      [:ul
       (for [[group subreqs] requirements]
         [:li
          (str
           (norm group) ": "
           (string/join
            ", "
            (for [[key value] subreqs]
              (str (norm key) " " value))))])]])
   (when-let [tags (:tags feat)]
     [:p.tags
      (for [tag tags]
        ^{:key tag}
        [:span.tag (norm tag)])])])

(defn- feats-view []
  [:div.columns
   [:div.column.is-2
    [:div.box>div.content
     [:h1.title "Feats"]
     [:p "Content:"]
     [:ul
      [:li [:button.is-link
            {:on-click (scroll-to "backgrounds")}
            "Backgrounds"]
       [:ul
        [:li [:button.is-link
              {:on-click (scroll-to "background-talents")}
              "Talents"]]
        [:li [:button.is-link
              {:on-click (scroll-to "background-techniques")}
              "Techniques"]]]]
      [:li [:button.is-link
            {:on-click (scroll-to "general-feats")}
            "General Feats"]
       [:ul
        [:li [:button.is-link
              {:on-click (scroll-to "general-talents")}
              "Talents"]]
        [:li [:button.is-link
              {:on-click (scroll-to "general-techniques")}
              "Techniques"]]]]]]]
   [:div.column
    [:div.box>div.content
     (let [feats (concat base-feats @custom-feats)
           background-feats (feats/feats+tag=>feats feats :background)
           background-talents (feats/feats+tag=>feats background-feats :talent)
           background-techniques (feats/feats+tag=>feats background-feats :techniques)
           general-feats (feats/feats-tag=>feats feats :background)
           general-talents (feats/feats+tag=>feats general-feats :talent)
           general-techniques (feats/feats+tag=>feats general-feats :techniques)]
       [:<>
        [:h4 {:name "backgrounds"} "Backgrounds"]
        (when (seq background-feats)
          [:<>
           (when (seq background-talents)
             [:<>
              [:h5 {:name "background-talents"} "Talents"]
              (for [feat (sort-by :name background-talents)]
                ^{:key (:id feat)}
                [print-feat feat])])
           (when (seq background-techniques)
             [:<>
              [:h5 {:name "background-techniques"} "Techniques"]
              (for [feat (sort-by :name background-techniques)]
                ^{:key (:id feat)}
                [print-feat feat])])])
        [:h4 {:name "general-feats"} "General feats"]
        (when (seq general-feats)
          [:<>
           (when (seq general-talents)
             [:<>
              [:h5 {:name "general-talents"} "Talents"]
              (for [feat (sort-by :name general-talents)]
                ^{:key (:id feat)}
                [print-feat feat])])
           (when (seq general-techniques)
             [:<>
              [:h5 {:name "general-techniques"} "Techniques"]
              (for [feat (sort-by :name general-techniques)]
                ^{:key (:id feat)}
                [print-feat feat])])])])]]])

(defn- print-equipment [equipment]
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

(defn- equipment-view []
  [:div.columns
   [:div.column.is-2
    [:div.box>div.content
     [:h1.title "Equipment"]
     [:p "Content:"]
     [:ul
      [:li [:button.is-link
            {:on-click (scroll-to "weapons")}
            "Weapons"]]
      [:li [:button.is-link
            {:on-click (scroll-to "armor")}
            "Armor"]]
      [:li [:button.is-link
            {:on-click (scroll-to "packs")}
            "Packs"]]
      [:li [:button.is-link
            {:on-click (scroll-to "items")}
            "Items"]]]]]
   [:div.column
    [:div.box>div.content
     (let [equipment (concat base-equipment @custom-equipment)
           grouped (group-by :slot equipment)
           sorted-weapons (->> grouped :weapon (sort-by :name))
           sorted-armor (->> grouped :armor (sort-by :inertia))]
       [:<>
        [:h4 {:name "weapons"} "Weapons"]
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
        [:h4 {:name "armor"} "Armor"]
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

(defn- characters-view []
  [:div.columns
   [:div.column.is-2
    [:div.box>div.content
     [:p "Characters:"]
     (for [character (vals (all-characters))]
       ^{:key character}
       [:button.button.is-fullwidth
        {:on-click #(reset! active-character character)
         :class (when (= @active-character character) "is-primary")}
        (get-in character [:bio :name])
        (when (nil? (:stats character))
          " [sample]")])
     [:hr]
     [:button.button.is-fullwidth.is-success
      #(do
         (reset! active-character nil)
         (reset! editing-character? true))
      "New Character"]]]
   [:div.column
    (when-let [character @active-character]
      [character-sheet character])
    (when @editing-character?
      [:h1 "TODO"])]])

(defn- app []
  [:section.section
   [navbar]
   [:div.block
    (when @current-view
      (when-let [view (-> @current-view :data :view)]
        [view @current-view]))]])

(def routes
  [["/"
    {:name ::index
     :view index-view}]
   ["/feats"
    {:name ::feats
     :view feats-view}]
   ["/equipment"
    {:name ::equipment
     :view equipment-view}]
   ["/characters"
    {:name ::characters
     :view characters-view}]
   #_["/npc-manager"
      {:name ::npc-manager
       :view npc-manager-view}]])

(def router (rf/router routes {:data {:coercion rss/coercion}}))

(defn on-navigate [m]
  (reset! current-view m))

(rfe/start! router on-navigate {:use-fragment true})
(rd/render [app] (js/document.getElementById "app"))