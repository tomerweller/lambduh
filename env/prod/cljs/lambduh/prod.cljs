(ns lambduh.prod
  (:require [lambduh.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
