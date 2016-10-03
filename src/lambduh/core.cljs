(ns lambduh.core
  (:require [reagent.core :as r]
            [clojure.string :as str]
            [cljs.js :refer [empty-state eval js-eval]]))

;; Debug
(enable-console-print!)

;; -------------------------
;; Views
(defonce state
         (r/atom {}))

(defn update-state!
  [str]
  (swap! state assoc :str str)
  (let [lines (str/split str "\n")
        exps (map #(cljs.reader/read-string %) lines)]
    (try
      (swap! state assoc :expression (into exps `(do)))
      (eval
        (empty-state)
        (into exps `(do))
        {:eval js-eval}
        #(swap! state assoc :result (:value %)))
      (catch
        js/Error e
        (js/console.error "Eval failed:" e)))))

(def ace-editor (r/adapt-react-class (aget js/deps "react-ace" "default")))

(defn root []
  (let [current-state @state]
    [:div
     [:h2 "Lambduhduh"]
     [:div#main-container
      [:div#editor-container
       [ace-editor
        {:value (:str current-state)
         :name "editor"
         :mode "clojure"
         :theme "twilight"
         :on-change #(update-state! %)}]]
      [:div#result-container
       [:p (with-out-str (cljs.pprint/pprint (:expression current-state)))]
       [:p (with-out-str (cljs.pprint/pprint (:result current-state)))]]]]))

;; -------------------------
;; Initialize app
(update-state! "(+ 1 2 3 4)")
(defn mount-root []
  (r/render [root] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
