(ns planning.map-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest is testing run-tests]])
            [planning.core :as p]
            [planning.utils :as u]
            [clojure.string :as cs]))

(def meadow-32x32x4
  "A text map in which there is varying terrain difficulty:
  * # is plains (easy)
  * ^ is mountains (hard)
  * ~ is water (easiest - maybe you have a boat)
  * \" \" is not traversable"
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
   "#####~##   ###^^################"
   "######~     ####^^##############"
   "######~     #^^^^###############"
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

(def terrain-cost {"#" 1 "^" 2 "~" 0.5 " " ##Inf})

(defn cost-fn [grid from to]
  (*
    (u/euclidian-distance from to)
    (terrain-cost (str (get-in grid to)) ##Inf)))

(def default-setup
  {:neighbors-fn (partial u/moore-neigbors meadow-32x32x4)
   :cost-fn      (partial cost-fn meadow-32x32x4)
   :heuristic-fn u/euclidian-distance})

(comment
  (->> {:start [0 0] :goal [31 31]} (into default-setup) p/A-star-search (add-path meadow-32x32x4))
  (->> {:start [0 0] :goal [31 31]} (into default-setup) p/dijkstra-search (add-path meadow-32x32x4))

  (->> {:start [8 3] :goal [31 31]} (into default-setup) p/A-star-search (add-path meadow-32x32x4))
  (->> {:start [8 3] :goal [31 31]} (into default-setup) p/dijkstra-search (add-path meadow-32x32x4))

  (->> {:start [8 3] :goal [14 17]} (into default-setup) p/A-star-search (add-path meadow-32x32x4))
  (->> {:start [8 3] :goal [14 17]} (into default-setup) p/dijkstra-search (add-path meadow-32x32x4)))

(deftest a-star-steps-lte-dijkstra-steps-tests
  (testing "Example-based: A* should take less steps than Dijkstra solution."
    (let [problem (into default-setup {:start [0 0] :goal [31 31]})
          ca (count (:visited (p/A-star-terminus problem)))
          cd (count (:visited (p/dijkstra-terminus problem)))]
      (is (= [226 930] [ca cd]))
      (is (< ca (/ cd 3))))
    (let [problem (into default-setup {:start [8 3] :goal [14 17]})]
      (is (<= (count (:visited (p/A-star-terminus problem)))
              (count (:visited (p/dijkstra-terminus problem))))))
    (let [problem (into default-setup {:start [8 3] :goal [31 31]})]
      (is (< (count (:visited (p/A-star-terminus problem)))
             (count (:visited (p/dijkstra-terminus problem))))))))


