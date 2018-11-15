(ns planning.app
  (:require [reagent.core :as reagent :refer [atom]]))

(defn render [state]
  (let [cell-dim 20
        {:keys [locked score high-score]} @state]
    [:div
     [:h1 "TetriClone"]
     ;[:svg {:width (* cell-dim 10) :height (* cell-dim 22)}
     ; (doall (for [i (range 10) j (range 22)]
     ;          [:rect { :key (str i ":" j) :x (* i cell-dim) :y (* j cell-dim)
     ;                  :width cell-dim :height cell-dim
     ;                  :stroke :red :fill (cond
     ;                                       (locked [i j]) :blue
     ;                                       (sc [i j]) :orange
     ;                                       :default :black) }]))]
     ;[:h4 (str "Score: " score)]
     ;[:h4 (str "High Score: " high-score)
     ; ]

     ;[:button #(prn (with-out-str (pprint state))) "dump state"]
     ]))

(when-let [app-context (. js/document (getElementById "app"))]
  (let [state (atom {})]
    (reagent/render-component
      [render state]
      app-context)))
