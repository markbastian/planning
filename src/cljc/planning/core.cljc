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

(defn dist [a b]
  (let [u (map - a b)]
    (Math/sqrt (reduce + (map * u u)))))

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

;Dijkstra's algorithm
(defn dijkstra-step [neighbors cost-fn {:keys [frontier cost] :as m}]
  (let [[current-state current-cost] (peek frontier)
        costs (->> (neighbors current-state)
                   (map (fn [next-state] [next-state (+ current-cost (cost-fn current-state next-state))]))
                   (remove (fn [[next-state next-cost]]
                             (when-let [existing-cost (cost next-state)]
                               (>= next-cost existing-cost))))
                   (apply conj {}))]
    (-> m
        (update :frontier #(-> % pop (into costs)))
        (update :cost into costs)
        (update :visited into (zipmap (keys costs) (repeat current-state))))))

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

(def height-map
  '[[1 1 1 1 1 1]
    [1 1 0 0 0 1]
    [1 1 1 1 0 1]
    [1 1 1 1 0 1]
    [1 0 0 0 0 1]
    [1 1 1 1 1 1]])

(defn mark-path [grid solution]
  (reduce (fn [g c] (assoc-in g c 'X)) grid solution))

(pp/pprint
  (mark-path
  height-map
  (greedy-bfs-search
  (fn [a] (filter #(some->> % (get-in height-map) pos?) (neighbors-8 a)))
  dist
  [0 0]
  [5 5])))

(pp/pprint
  (mark-path
    height-map
    [[0 0] [1 1] [2 2] [3 3] [2 3] [3 2] [3 1] [2 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]]))

(pp/pprint
  (mark-path
    height-map
    [[0 0] [1 1] [2 2] [3 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]]))

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

(add-path meadow-32x32x4 (breadth-first-search
                           [7 5]
                           [31 31]
                           (fn [c]
                             (let [n (neighbors c)]
                               (filter #(= \# (get-in meadow-32x32x4 %)) n)))))
