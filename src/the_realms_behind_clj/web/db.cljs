(ns the-realms-behind-clj.web.db
  (:require ["pouchdb" :as pouchdb]
            [clojure.edn :as edn]
            [the-realms-behind-clj.characters :as characters]
            [the-realms-behind-clj.resources :as resources]))

(def ACTIONS "action")
(def CHARACTERS "character")
(def EQUIPMENT "equipment")
(def FEATS "feat")

(defn init-db
  ([name]
   (new pouchdb name))
  ([name opts]
   (new pouchdb name opts)))

(def db (init-db "the-realms-behind"))

(defn unmarshal-doc [doc]
  (edn/read-string (:-value (js->clj doc :keywordize-keys true))))

(defn marshal-doc [base value to-index]
  (clj->js
   (merge base
          (select-keys value to-index)
          {:-value (pr-str value)})))

(defn save-doc
  ([db id value]
   (save-doc db id value []))
  ([db id value to-index]
   (.put db (marshal-doc {:_id (str id)} value to-index))))

(defn upsert-doc
  ([db id value]
   (upsert-doc db id value []))
  ([db id value to-index]
   (.catch (save-doc db id value to-index)
           (fn [e]
             (if (= 409 (.-status e))
               (.then (.get db id)
                      #(.put db (marshal-doc (js->clj % {:keywordize-keys true})
                                             value to-index)))
               (throw e))))))

(defn resolve-id [db id]
  (.then (.get db id) unmarshal-doc))

(defn remove-id! [db id]
  (-> (.get db id)
      (.then #(.remove db %))))

(defn normalize-results [results]
  (js->clj (.-rows results) :keywordize-keys true))

(defn fetch-docs
  ([db]
   (fetch-docs db {}))
  ([db opts]
   (.then (.allDocs ^js/Object db (clj->js opts)) normalize-results)))

(defn typed-uuid [type-name]
  (fn []
    (str type-name "/" (random-uuid))))

(def action-uuid (typed-uuid ACTIONS))
(def character-uuid (typed-uuid CHARACTERS))
(def equipment-uuid (typed-uuid EQUIPMENT))
(def feat-uuid (typed-uuid FEATS))

(defn typed-fetch [type-name]
  (fn do-typed-fetch
    ([db] (do-typed-fetch db {}))
    ([db opts]
     (.then (fetch-docs
             db
             (merge
              {:startkey type-name
               :endkey (str type-name "\uffff")
               :include_docs true}
              opts))
            #(map (comp edn/read-string :-value :doc) %)))))

(def fetch-actions (typed-fetch ACTIONS))
(def fetch-characters (typed-fetch CHARACTERS))
(def fetch-equipment (typed-fetch EQUIPMENT))
(def fetch-feats (typed-fetch FEATS))

(defn all-actions []
  (.then (fetch-actions db)
         #(concat (resources/actions) %)))

(defn all-characters []
  (.then (fetch-characters db)
         #(vals
           (reduce
            (fn [characters character]
              (assoc characters (:id character) character))
            characters/sample-characters
            %))))

(defn all-equipment []
  (.then (fetch-equipment db)
         #(concat (resources/equipment) %)))

(defn all-feats []
  (.then (fetch-feats db)
         #(concat (resources/feats) %)))
