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

(def code-mirror (r/adapt-react-class (aget js/deps "react-codemirror")))

(defn root []
  (let [current-state @state]
    [:div
     [:h2 "Lambduhduh"]
     ;TODO: use this codemirror component
     [code-mirror {:value (:str current-state)
                   :options {:mode "clojure" :lineNumbers true}
                   :on-change #(update-state! %)}]
     [:textarea#codearea
      {:value (:str current-state)
       :on-change #(update-state! (-> % .-target .-value))}]
     [:p (with-out-str (cljs.pprint/pprint (:expression current-state)))]
     [:p (with-out-str (cljs.pprint/pprint (:result current-state)))]]))

;; -------------------------
;; Initialize app
(update-state! "(+ 1 2 3 4)")
(defn mount-root []
  (r/render [root] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
