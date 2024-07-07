(ns the-realms-behind-clj.web.utils)

(defn e->value [e]
  (-> e .-target .-value))
