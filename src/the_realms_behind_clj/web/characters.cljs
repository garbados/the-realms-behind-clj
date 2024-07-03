(ns the-realms-behind-clj.web.characters 
  (:require [reagent.core :as r]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.specs :as specs]
            [the-realms-behind-clj.web.db :as db]
            [the-realms-behind-clj.web.equipment :refer [print-equipment
                                                         print-equipment-short]]
            [the-realms-behind-clj.web.feats :refer [print-feat]]
            [the-realms-behind-clj.web.text :refer [norm prompt-text
                                                    prompt-textarea]]
            [reitit.frontend.easy :as rfe]))

(def overflow-area
  {:style {:overflow "scroll"
           :max-height 700}})

(defn get-stats [character]
  (or (:stats character) (characters/base-stats character)))

(defn disj-equipment [inv single-equipment]
  (vec (disj (set inv) single-equipment)))

(defn move-equipment-to
  [-character k-from k-to single-equipment]
  (swap! -character update k-from disj-equipment single-equipment)
  (swap! -character update k-to conj single-equipment))

(defn move-equipment-button
  [-character k-from k-to single-equipment]
  [:button.button.is-fullwidth.is-outlined
   {:class (case k-to
             :equipped "is-primary"
             :at-hand "is-info"
             :inventory "is-warning")
    :on-click
    #(move-equipment-to -character k-from k-to single-equipment)}
   (str "Move to " (case k-to
                     :equipped "Equipped"
                     :at-hand "At-Hand"
                     :inventory "Inventory"))])

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

(defn- print-stats
  ([character]
   [print-stats character {}])
  ([character {:keys [explain?]}]
   [:div.content
    [:h5 "Stats"]
    (let [stats (get-stats character)]
      [:<>
       [:table.table.is-fullwidth
        [:thead
         [:tr
          [:th "X"]
          [:th "Stat"]
          (when explain?
            [:th "Formula"])]]
        [:tbody
         (let [{:keys [max-health]} stats
               [shallow deep] max-health]
           [:tr
            [:td (str shallow " Shallow, " deep " Deep")]
            [:td "Health"]
            (when explain?
              [:td "Resilience ~ 1x Shallow, 2x Deep"])])
         (doall
          (for [[stat formula]
                [[:will "2x Resolve"]
                 [:fortune "2x Luck"]
                 [:draw "3x Insight"]
                 [:speed "2 + Athletics"]
                 [:initiative "2x Awareness"]]
                :let [x (get stats stat)]]
            ^{:key stat}
            [:tr
             [:td x]
             [:td (norm stat)]
             (when explain?
               [:td formula])]))]]
       [:table.table.is-fullwidth
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
            [:td [:em (norm skill)]]])]]])]))

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
         [print-equipment-short equipment])
       [:h5 "At-Hand ("  (reduce + 0 (map :bulk at-hand)) ")"]
       (for [equipment at-hand]
         ^{:key (:id equipment)}
         [print-equipment-short equipment])
       [:h5 "Inventory (" (reduce + 0 (map :bulk inventory)) ")"]
       (for [equipment inventory]
         ^{:key (:id equipment)}
         [print-equipment-short equipment])])
    [:div.column.is-3>div.content
     [:h5 "Feats"]
     (for [feat (sort-by :name (:feats character))
           :let [trimmed (select-keys feat [:name :level :description])]]
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

(defn- edit-feats
  ([-character]
   (let [-feats (r/atom [])]
     (.then (db/all-feats)
            #(reset! -feats %))
     [edit-feats -character -feats]))
  ([-character -feats]
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
       (for [feat @-feats
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
             "Gain (+" cost ")"]]]])]]]]))

(defn- edit-equipment [-character]
  [:div.columns
   [:div.column.is-6
    (let [owned-equipment
          (sort
           characters/sort-equipment
           (characters/carried-equipment @-character))]
      [:div.content
       [:h3 "Owned equipment"]
       [:div
        overflow-area
        (for [single-equipment owned-equipment]
          ^{:key (:id single-equipment)}
          [print-equipment single-equipment
           (let [cost (characters/wealth-cost
                       (:level single-equipment))]
             [:div.level
              [:div.level-item
               [:button.button.is-fullwidth.is-danger
                {:on-click
                 #(swap! -character update :inventory
                         disj-equipment single-equipment)}
                "Take (-" cost ")"]]
              [:div.level-item
               [:button.button.is-fullwidth.is-warning
                {:on-click
                 #(do
                    (swap! -character update :gold + cost)
                    (swap! -character update :inventory
                           disj-equipment single-equipment))}
                "Sell"]]])])]])]
   [:div.column.is-6
    [:div.content
     [:h3 "Available equipment"]
     (for [[group equipment]
           (->> (db/all-equipment)
                (group-by :slot)
                (seq)
                (sort-by first))]
       ^{:key group}
       [:<>
        [:h5 "Slot: " (norm group)]
        [:div
         overflow-area
         (for [single-equipment (sort-by :name equipment)]
           ^{:key (:id single-equipment)}
           [print-equipment single-equipment
            (let [cost (characters/wealth-cost
                        (:level single-equipment))]
              [:div.level
               [:div.level-item
                [:button.button.is-fullwidth.is-success
                 {:disabled
                  (> cost (:gold @-character))
                  :on-click
                  #(do
                     (swap! -character update :gold - cost)
                     (swap! -character update :inventory conj single-equipment))}
                 "Buy"]]
               [:div.level-item
                [:button.button.is-fullwidth.is-info
                 {:on-click
                  #(swap! -character update :inventory conj single-equipment)}
                 "Give (+" cost ")"]]])])]])]]])

(defn- edit-carrying [-character]
  (let [[equipped-max-bulk
         at-hand-max-bulk
         inventory-max-bulk]
        (for [group [:equipped :at-hand :inventory]]
          (characters/get-max-bulk @-character group))
        [equipped-bulk
         at-hand-bulk
         inventory-bulk]
        (for [k [:equipped :at-hand :inventory]]
          (characters/carrying-bulk (select-keys @-character [k])))]
    [:div.columns
     [:div.column.is-6>div.content
      [:h3 "Equipped"
       " "
       [:span.subtitle [:em "(bulk " equipped-bulk " / " equipped-max-bulk ")"]]]
      (let [k-from :equipped]
        (for [single-equipment (get @-character k-from [])]
          ^{:key (:id single-equipment)}
          [print-equipment single-equipment
           [:div.level
            (doall
             (for [k-to [:at-hand :inventory]]
               ^{:key k-to}
               [:div.level-item
                [move-equipment-button -character k-from k-to single-equipment]]))]]))
      [:h3 "At-Hand"
       " "
       [:span.subtitle [:em "(bulk " at-hand-bulk " / " at-hand-max-bulk ")"]]]
      (let [k-from :at-hand]
        (for [single-equipment (get @-character k-from [])]
          ^{:key (:id single-equipment)}
          [print-equipment single-equipment
           [:div.level
            (doall
             (for [k-to [:equipped :inventory]]
               ^{:key k-to}
               [:div.level-item
                [move-equipment-button -character k-from k-to single-equipment]]))]]))]
     [:div.column.is-6
      [:div.content
       [:h3 "Inventory"
       " "
       [:span.subtitle [:em "(bulk " inventory-bulk " / " (* 2 inventory-max-bulk) ")"]]]
       (let [k-from :inventory]
         (for [single-equipment (get @-character k-from [])]
           ^{:key (:id single-equipment)}
           [print-equipment single-equipment
            [:div.level
             (doall
              (for [k-to [:equipped :at-hand]]
                ^{:key k-to}
                [:div.level-item
                 [move-equipment-button -character k-from k-to single-equipment]]))]]))]]]))

(defn reset-characters! [-characters]
  (.then (db/all-characters)
         #(reset! -characters %)))

(defn- edit-character [-characters -character -editing?]
  (let [-name (r/atom (get-in @-character [:bio :name] ""))
        -description (r/atom (get-in @-character [:bio :description] ""))
        -image-url (r/atom (get-in @-character [:bio :image-url] ""))]
    [:div.box
     [:div.content>h1
      (if (:id @-character)
        "Edit Character"
        "New Character")]
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
     [:div.content
      [:h1 "XP Total: " (characters/base-xp @-character)]
      [:p
       "XP Free: "
       [:span
        [:input
         {:size 4
          :type "number"
          :on-change
          #(swap! -character assoc :experience (-> % .-target .-value))
          :value (:experience @-character 0)}]]]]
     [:div.columns
      [:div.column.is-6
       [edit-attributes -character]
       [print-stats @-character {:explain? true}]]
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
         :value (:gold @-character 0)}]]]
     [edit-equipment -character]
     [:hr]
     [edit-carrying -character]
     [:hr]
     [:div.content
      [:p
       [:button.button.is-fullwidth.is-success
        {:on-click
         #(let [uuid (db/character-uuid)
                character
                (assoc
                 @-character
                 :id uuid
                 :bio {:name @-name
                       :description @-description
                       :image-url @-image-url}
                 :stats (characters/base-stats @-character))]
            (.catch
             (db/save-doc db/db uuid character)
             (fn [e] (println e)))
            (reset-characters! -characters)
            (reset! -character character)
            (reset! -editing? false))}
        "Save Character"]]
      (when-let [id (:id @-character)]
        (when (some? (re-matches #"^character/.+$" id))
          [:p
           [:button.button.is-fullwidth.is-danger
            {:on-click
             #(let [id (:id @-character (db/character-uuid))]
                (.then
                 (db/remove-id! db/db id)
                 (fn [] (reset-characters! -characters)))
                (reset! -character characters/base-character)
                (reset! -editing? true))}
            "Delete Character"]]))]]))

(defn first-by-id [l id]
  (first (filter #(= id (:id %)) l)))

(defn characters-view
  ([_]
   (let [-characters (r/atom [])
         -character (r/atom characters/base-character)
         -editing? (r/atom true)]
     (reset-characters! -characters)
     (.then (db/all-characters)
            #(reset! -characters %))
     [characters-view -characters -character -editing?]))
  ([-characters -character -editing?]
   [:<>
    #_[:p (pr-str @-character)]
    [:div.columns
     [:div.column.is-2
      [:div.box>div.content
       [:p "Characters:"]
       (doall
        (for [character @-characters]
          ^{:key (:id character)}
          [:button.button.is-fullwidth
           {:on-click #(do
                         (reset! -character character)
                         (reset! -editing? false))
            :class (when (= @-character character) "is-primary")}
           (get-in character [:bio :name])
           (when (nil? (:stats character))
             " [sample]")]))
       [:hr]
       [:button.button.is-fullwidth.is-success
        {:on-click
         #(do
            (reset! -character characters/base-character)
            (reset! -editing? true))}
        "New Character"]]]
     [:div.column
      (when-let [character (and (not @-editing?) @-character)]
        [:<>
         [:p
          [:button.button.is-fullwidth.is-info
           {:on-click #(do
                         (swap! -character dissoc :id)
                         (reset! -editing? true))}
           "Use as Template for New Character"]]
         [character-sheet character]])
      [edit-character -characters -character -editing?]]]]))
