(ns the-realms-behind-clj.web.core
  (:require [reagent.core :as r]
            [reagent.dom :as rd]
            [reitit.coercion.spec :as rss]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [the-realms-behind-clj.web.characters :refer [characters-view]]
            [the-realms-behind-clj.web.equipment :refer [equipment-view]]
            [the-realms-behind-clj.web.feats :refer [feats-view]]))

(def source-url "https://github.com/garbados/the-realms-behind-clj/")
(def intro-url (str source-url "blob/main/doc/intro.md"))

(def current-view (r/atom :loading))

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

(defn- app []
  [:section.section
   [navbar]
   [:div.block
    (when @current-view
      (when-let [view (-> @current-view :data :view)]
        [view @current-view]))]])

(def router
  (rf/router
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
        :view npc-manager-view}]]
   {:data {:coercion rss/coercion}}))

(defn on-navigate [m]
  (reset! current-view m))

(rfe/start! router on-navigate {:use-fragment true})
(rd/render [app] (js/document.getElementById "app"))
