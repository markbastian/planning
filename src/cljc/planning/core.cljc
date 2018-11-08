(ns planning.core
  (:require [clojure.string :as cs]
            #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer
                      [priority-map priority-map-by]]
               :cljs [tailrecursion.priority-map :refer
                      [priority-map priority-map-by]])
            [clojure.pprint :as pp]))

(defn neighbors [[x y]]
  (let [i ((juxt inc identity dec identity) x)
        j ((juxt identity inc identity dec) y)]
    (map vector i j)))

(defn neighbors-8 [[x y]]
  (let [i ((juxt inc inc identity dec dec dec identity inc) x)
        j ((juxt identity inc inc inc identity dec dec dec) y)]
    (map vector i j)))

(defn euclidian-distance [a b]
  (let [u (map - a b)]
    (Math/sqrt (reduce + (map * u u)))))

(defn manhattan-distance [a b]
  (reduce + (map (comp #(Math/abs %) -) a b)))

(defn mark-path [grid solution]
  (reduce (fn [g c] (assoc-in g c 'X)) grid solution))

(def empty-queue #?(:clj  (clojure.lang.PersistentQueue/EMPTY)
                    :cljs (cljs.core.PersistentQueue/EMPTY)))

;;See page 29 of Lavalle - stf = state transition function. Also, just "neighbors"
;Note that this implementation suffers from opacity and inflexiblity
(defn search [frontier initial-state goal stf]
  (loop [Q (conj frontier initial-state) visited {initial-state nil}]
    (cond
      (= (peek Q) goal) (vec (take-while some? (iterate visited goal)))
      (empty? Q) nil
      :else (let [neighbors (remove #(contains? visited %) (stf (peek Q)))]
              (recur
                (into (pop Q) neighbors)
                (into visited (zipmap neighbors (repeat (peek Q)))))))))

(def dfs (partial search []))
(def bfs (partial search empty-queue))

(defn recover-path [goal visited]
  (vec (reverse (take-while some? (iterate visited goal)))))

;Breadth first search
(defn search-step [neighbors {:keys [frontier visited] :as m}]
  (let [next-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors next-state))]
    (-> m
        (update :frontier #(-> % pop (into new-neighbors)))
        (update :visited into (zipmap new-neighbors (repeat next-state))))))

(defn search-seq [q start neighbors]
  (->> {:frontier (conj q start) :visited {start nil}}
       (iterate (partial search-step neighbors))
       (take-while (comp seq :frontier))))

(defn iterative-search [q start goal neighbors]
  (some->> (search-seq q start neighbors)
           (some (fn [{:keys [visited]}] (when (visited goal) visited)))
           (recover-path goal)))

(def breadth-first-search (partial iterative-search empty-queue))
(def depth-first-search (partial iterative-search []))

(defn compute-costs [current-state neighbors-fn current-costs cost-fn]
  (for [neighbor (neighbors-fn current-state)
        :let [new-cost (+ (current-costs current-state) (cost-fn current-state neighbor))
              existing-cost (current-costs neighbor)]
        :when (or (nil? existing-cost) (< new-cost existing-cost))]
    [neighbor new-cost]))

;Dijkstra's algorithm
(defn dijkstra-step [neighbors cost-fn {:keys [frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (compute-costs current-state neighbors cost cost-fn)]
    (-> m
        (update :frontier #(-> % pop (into costs)))
        (update :cost into costs)
        (update :visited into (map (fn [[c]] [c current-state]) costs)))))

(defn dijkstra-seq [neighbors cost-fn start]
  (->> {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}}
       (iterate (partial dijkstra-step neighbors cost-fn))
       (take-while (comp seq :frontier))))

(defn dijkstra-path [neighbors cost-fn start goal]
  (->> (dijkstra-seq neighbors cost-fn start)
       (some (fn [{:keys [visited]}] (when (visited goal) visited)))
       (recover-path goal)))

;Greedy best first search
(defn greedy-bfs-step [neighbors heuristic {:keys [frontier visited] :as m}]
  (let [[current-state _] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))]
    (-> m
        (update :frontier #(-> % pop (into (for [n new-neighbors] [n (heuristic n)]))))
        (update :visited into (zipmap new-neighbors (repeat current-state))))))

(defn greedy-bfs-seq [neighbors heuristic start]
  (->> {:frontier (priority-map start 0) :visited {start nil}}
       (iterate (partial greedy-bfs-step neighbors heuristic))
       (take-while (comp seq :frontier))))

(defn greedy-bfs-search [neighbors heuristic start goal]
  (some->> (greedy-bfs-seq neighbors (partial heuristic goal) start)
           (some (fn [{:keys [visited]}] (when (visited goal) visited)))
           (recover-path goal)))

;A* algorithm
(defn A*-step [neighbors cost-fn heuristic {:keys [frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (compute-costs current-state neighbors cost cost-fn)
        estimates (map (fn [[s c]] [s (+ c (heuristic s))]) costs)]
    (-> m
        (update :frontier #(-> % pop (into estimates)))
        (update :cost into costs)
        (update :visited into (map (fn [[c]] [c current-state]) costs)))))

(defn A*-seq [neighbors cost-fn heuristic start]
  (->> {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}}
       (iterate (partial A*-step neighbors cost-fn heuristic))
       (take-while (comp seq :frontier))))

(defn A*-search [neighbors cost-fn heuristic start goal]
  (some->> (A*-seq neighbors cost-fn (partial heuristic goal) start)
           (some (fn [{:keys [visited]}] (when (visited goal) visited)))
           (recover-path goal)))

(def height-map
  '[[1 1 1 1 1 1]
    [1 1 0 0 0 1]
    [1 1 1 1 0 1]
    [1 1 1 1 0 1]
    [1 0 0 0 0 1]
    [1 1 1 1 1 1]])

(->>
  (dijkstra-seq
    (fn [a] (filter (fn [c] (when-some [h (get-in height-map c)] (pos? h))) (neighbors-8 a)))
    euclidian-distance
    [0 0])
  (map (comp first :frontier))
  (take 40))

(time
  (pp/pprint
    (mark-path
      height-map
      (dijkstra-path
        (fn [a] (filter (fn [c] (when-some [h (get-in height-map c)] (pos? h))) (neighbors-8 a)))
        euclidian-distance
        [0 0]
        [5 5]))))

(->>
  (A*-seq
    (fn [a] (filter (fn [c] (when-some [h (get-in height-map c)] (pos? h))) (neighbors-8 a)))
    euclidian-distance
    (partial euclidian-distance [5 5])
    [0 0])
  (map (comp first :frontier))
  (take 23))

(time
  (pp/pprint
    (mark-path
      height-map
      (A*-search
        (fn [a] (filter (fn [c] (when-some [h (get-in height-map c)] (pos? h))) (neighbors-8 a)))
        euclidian-distance
        euclidian-distance
        [0 0]
        [5 5]))))

(pp/pprint
  (mark-path
    height-map
    (greedy-bfs-search
      (fn [a] (filter (fn [c] (when-some [h (get-in height-map c)] (pos? h))) (neighbors-8 a)))
      euclidian-distance
      [0 0]
      [5 5])))

;Next step is https://www.redblobgames.com/pathfinding/a-star/introduction.html#greedy-best-first



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

(defn add-path [text-map path]
  (->> path
       (reduce
         (fn [g c] (assoc-in g c "X"))
         (mapv vec text-map))
       (mapv cs/join)))

(time
  (pp/pprint
    (add-path
      meadow-32x32x4
      (A*-search
        (fn [a] (filter (fn [c] (= "#" (str (get-in meadow-32x32x4 c)))) (neighbors-8 a)))
        euclidian-distance
        euclidian-distance
        [4 20]
        [31 11]))))

(time
  (A*-search
    (fn [a] (filter (fn [c] (= "#" (str (get-in meadow-32x32x4 c)))) (neighbors-8 a)))
    euclidian-distance
    euclidian-distance
    [4 20]
    [31 11]))

(time
  (pp/pprint
    (add-path
      meadow-32x32x4
      (dijkstra-path
        (fn [a] (filter (fn [c] (= "#" (str (get-in meadow-32x32x4 c)))) (neighbors-8 a)))
        euclidian-distance
        [4 20]
        [31 11]))))

(time
  (dijkstra-path
    (fn [a] (filter (fn [c] (= "#" (str (get-in meadow-32x32x4 c)))) (neighbors-8 a)))
    euclidian-distance
    [4 20]
    [31 11]))

(add-path meadow-32x32x4 (breadth-first-search
                           [7 5]
                           [31 31]
                           (fn [c]
                             (let [n (neighbors c)]
                               (filter #(= \# (get-in meadow-32x32x4 %)) n)))))
