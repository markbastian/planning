(ns planning.meadow-test
  (:require [clojure.test :refer :all]
            [clojure.string :as cs]
            [planning.core :as p]
            [planning.utils :as u]))

(defn add-path [text-map path]
  (->> path
       (reduce
         (fn [g c] (assoc-in g c "X"))
         (mapv vec text-map))
       (mapv cs/join)))

(def meadow-32x32x4
  ["################################"
   "#######################     ####"
   "######################       ###"
   "#####################        ###"
   "##################     ###  ####"
   "#################     ##########"
   "#################     ##########"
   "#################     ##########"
   "#####  ##########    ###########"
   "####    ##########  ############"
   "###      #######################"
   "###      #######################"
   "####    ########################"
   "################################"
   "################################"
   "################################"
   "################################"
   "################################"
   "########   #####################"
   "#######     ####################"
   "#######     ####################"
   "#######    #####################"
   "########  ######################"
   "################################"
   "################################"
   "##########   ###################"
   "#########     ##################"
   "#########     ##################"
   "##########    ##################"
   "###########    #################"
   "###########    #################"
   "############  ##################"])

(defn neighbors [a]
  (filter (fn [c] (= "#" (str (get-in meadow-32x32x4 c)))) (u/moore-neigbors a)))

(def A*-meadow-search (partial p/A*-search neighbors u/euclidian-distance u/euclidian-distance))
(def dijkstra-meadow-search (partial p/dijkstra-path neighbors u/euclidian-distance))
(def bfs-meadow-search (partial p/breadth-first-search neighbors))
(def greedy-meadow-search (partial p/greedy-bfs-search neighbors u/euclidian-distance))

(->>
  (A*-meadow-search {:start [4 20] :goal [31 11]})
  (add-path meadow-32x32x4))

(->>
  (dijkstra-meadow-search {:start [4 20] :goal [31 11]})
  (add-path meadow-32x32x4))

(->>
  (greedy-meadow-search {:start [4 20] :goal [31 11]})
  (add-path meadow-32x32x4))

(->>
  (bfs-meadow-search {:start [4 20] :goal [31 11]})
  (add-path meadow-32x32x4))

(->>
  (A*-meadow-search {:start [8 0] :goal [31 31]})
  (add-path meadow-32x32x4))

(->>
  (dijkstra-meadow-search {:start [8 0] :goal [31 31]})
  (add-path meadow-32x32x4))

(->>
  (greedy-meadow-search {:start [8 0] :goal [31 31]})
  (add-path meadow-32x32x4))

(->>
  (bfs-meadow-search {:start [8 0] :goal [31 31]})
  (add-path meadow-32x32x4))