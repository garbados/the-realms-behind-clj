(ns the-realms-behind-clj.web.feats 
  (:require [clojure.string :as string]
            [reagent.core :as r]
            [the-realms-behind-clj.feats :as feats]
            [the-realms-behind-clj.web.db :as db]
            [the-realms-behind-clj.web.nav :refer [scroll-to]]
            [the-realms-behind-clj.web.text :refer [norm]]
            [the-realms-behind-clj.resources :as resources]))

(defn print-reqs [requirements]
  [:ul
   (for [[group subreqs] (select-keys requirements [:attributes :skills :feats])]
     ^{:key group}
     [:li
      (str
       (norm group) ": "
       (string/join
        ", "
        (for [[key value] subreqs]
          (str (norm key) " " value))))])])

(defn print-feat
  [feat & extra]
  [:div.box>div.content
   [:h5 [:strong (:name feat)] " " [:em "(level " (:level feat) ")"]]
   [:p (:description feat)]
   (when (-> feat :tags :technique)
     [:<>
      [:p [:strong "Technique"]]
      [:p "Cost: "
       (->> (seq (:cost feat))
            (map
             (fn [[fungible x]]
               (str x " " (norm fungible))))
            (string/join "; "))]])
   (when-let [features (get-in feat [:effect :action :features])]
     [:ul
      (doall
       (for [feature features]
         [:li (:name feature) ": " (:description feature)]))])
   (when (-> feat :tags :buildable)
     [:<>
      [:p [:strong "[Buildable]"] " " [:em "Features:"]]
      (let [groups (get-in feat [:effect :features])
            features
            (->> (resources/features)
                 (filter (fn [{:keys [tags]}]
                           (some
                            (fn [group]
                              (or (tags :general)
                                  (tags group)))
                            groups)))
                 (sort-by :name)
                 (sort-by :level))]
        [:ul
         (doall
          (for [feature features]
            ^{:key (:id feature)}
            [:li
             [:p
              [:strong (:name feature)]
              " "
              [:em "(level " (:level feature) ")"]
              ": "
              (:description feature)]]))])])
   (when-let [requirements (:requirements feat)]
     [:<>
      [:p [:em "Requires:"]]
      [print-reqs requirements]
      (when-let [or-reqs (:or requirements)]
        [:<>
         [:p "Or:"]
         [:ul
          (doall
           (for [req or-reqs]
             ^{:key req}
             [:li
              [print-reqs req]]))]])])
   (when-let [tags (:tags feat)]
     [:p.tags
      (doall
       (for [tag tags]
         ^{:key tag}
         [:span.tag (norm tag)]))])
   extra])

(defn feats-view
  ([_]
   (let [-feats (r/atom [])]
     (.then (db/all-feats)
            #(reset! -feats %))
     [feats-view _ -feats]))
  ([_ -feats]
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
      (let [feats @-feats
            background-feats (feats/feats+tag=>feats feats :background)
            background-talents (feats/feats+tag=>feats background-feats :talent)
            background-techniques (feats/feats+tag=>feats background-feats :technique)
            general-feats (feats/feats-tag=>feats feats :background)
            general-talents (feats/feats+tag=>feats general-feats :talent)
            general-techniques (feats/feats+tag=>feats general-feats :technique)]
        [:<>
         [:h4 {:name "backgrounds"} "Backgrounds"]
         (when (seq background-feats)
           [:<>
            (when (seq background-talents)
              [:<>
               [:h5 {:name "background-talents"} "Talents"]
               (doall
                (for [feat (sort-by :name background-talents)]
                  ^{:key (:id feat)}
                  [print-feat feat]))])
            (when (seq background-techniques)
              [:<>
               [:h5 {:name "background-techniques"} "Techniques"]
               (doall
                (for [feat (sort-by :name background-techniques)]
                  ^{:key (:id feat)}
                  [print-feat feat]))])])
         [:h4 {:name "general-feats"} "General feats"]
         (when (seq general-feats)
           [:<>
            (when (seq general-talents)
              [:<>
               [:h5 {:name "general-talents"} "Talents"]
               (doall
                (for [feat (sort-by :name general-talents)]
                  ^{:key (:id feat)}
                  [print-feat feat]))])
            (when (seq general-techniques)
              [:<>
               [:h5 {:name "general-techniques"} "Techniques"]
               (doall
                (for [feat (sort-by :name general-techniques)]
                  ^{:key (:id feat)}
                  [print-feat feat]))])])])]]]))
