(ns the-realms-behind-clj.web.core
  (:require [the-realms-behind-clj.resources :as resources]
            [reagent.core :as r]
            [reagent.dom :as rd]))

(def state (r/atom :characters))

(def base-feats (resources/feats))
(def base-equipment (resources/equipment))
(def custom-feats (r/atom {}))
(def custom-equipment (r/atom {}))
(def custom-characters (r/atom {}))

(defn- navbar []
  [:div.level
   [:div.level-left
    [:div.level-item
     [:h1.title "The Realms Behind"]]
    [:div.level-item
     [:a.button.is-primary.is-light
      {:href "#TODO"}
      "Introduction"]]
    [:div.level-item
     [:button.button.is-info
      {:on-click #(reset! state :feats)}
      "Feats"]]
    [:div.level-item
     [:button.button.is-info
      {:on-click #(reset! state :equipment)}
      "Equipment"]]
    [:div.level-item
     [:button.button.is-primary
      {:on-click #(reset! state :characters)}
      "Characters"]]
    [:div.level-item
     [:button.button.is-primary
      {:on-click #(reset! state :npc-manager)}
      "NPC Manager"]]]
   [:div.level-right
    [:div.level-item
     [:a.button.is-info.is-light
      {:href "#TODO"}
      "Credits"]]
    [:div.level-item
     [:p.subtitle
      [:strong "Made with love by DFB"]]]]])

(defn- app []
  [:section.section
   [navbar]
   [:hr]
   [:div.block
    (case @state
      :feats [feats])]])

(rd/render [app] (js/document.getElementById "app"))