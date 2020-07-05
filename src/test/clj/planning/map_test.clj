(ns planning.map-test
  (:require [clojure.test :refer :all]
            [planning.core :as p]
            [planning.utils :as u]
            [clojure.string :as cs]))

(def meadow-32x32x4
  ["################################"
   "#######################     ####"
   "######################       ###"
   "#####################        ###"
   "##################     ###  ####"
   "#################     ##########"
   "#################     ##########"
   "#################     ##########"
   "#####~~##########    ###########"
   "####~~~~##########  ############"
   "###~~~~~~#######################"
   "###~~~~~~#######################"
   "####~~~~#######^^^^#############"
   "######~#####^^^^^^^^^^##########"
   "#####~#####^^^^^^^^^^^^#########"
   "####~########^^^^^^#############"
   "####~############^^^^###########"
   "#####~##########^^##############"
   "#####~##   ###^^^###############"
   "######~     ####^^##############"
   "######~     ##^^################"
   "######~    #####################"
   "#######~  ######################"
   "########~~######################"
   "################################"
   "##########   ###################"
   "#########     ##################"
   "#########     ##################"
   "##########    ##################"
   "###########    #################"
   "###########    #################"
   "############  ##################"])

(defn add-path [text-map path]
  (->> path
       (reduce
         (fn [g c] (assoc-in g c "X"))
         (mapv vec text-map))
       (mapv cs/join)))

(def terrain-cost
  {"#" 1
   "^" 2
   "~" 0.5
   " " ##Inf})

(defn cost-fn [grid from to]
  (*
    (u/euclidian-distance from to)
    (terrain-cost (str (get-in grid to)) ##Inf)))

(defn A*-meadow-search [m]
  (p/A-star-search
    (assoc m
      :neighbors (partial u/moore-neigbors meadow-32x32x4)
      :cost-fn (partial cost-fn meadow-32x32x4)
      :heuristic-fn u/euclidian-distance)))

(defn dijkstra-meadow-search [m]
  (p/dijkstra-search
    (assoc m
       :neighbors (partial u/moore-neigbors meadow-32x32x4)
       :cost-fn (partial cost-fn meadow-32x32x4))))

(->>
  (A*-meadow-search {:start [8 0] :goal [31 31]})
  (add-path meadow-32x32x4))

(->>
  (A*-meadow-search {:start [0 0] :goal [31 31]})
  (add-path meadow-32x32x4))

(->>
  (dijkstra-meadow-search {:start [0 0] :goal [31 31]})
  (add-path meadow-32x32x4))