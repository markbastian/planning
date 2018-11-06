(ns planning.core
  (:require [clojure.string :as cs]
            #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer
                      [priority-map priority-map-by]]
               :cljs [tailrecursion.priority-map :refer
                      [priority-map priority-map-by]])))

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn expand-search [neighbors {:keys [frontier visited] :as m}]
  (let [next-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors next-state))]
    (-> m
        (update :frontier #(-> % pop (into new-neighbors)))
        (update :visited into (zipmap new-neighbors (repeat next-state))))))

(defn search-seq [q start neighbors]
  (->> {:frontier (conj q start) :visited {start nil}}
       (iterate (partial expand-search neighbors))
       (take-while (comp seq :frontier))))

(defn iterative-search [q start goal neighbors]
  (some->> (search-seq q start neighbors)
           (some (fn [{:keys [visited]}] (when (visited goal) visited)))
           (recover-path goal)))

(def breadth-first-search (partial iterative-search empty-queue))
(def depth-first-search (partial iterative-search []))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn dijkstra-step [neighbors cost-fn {:keys [frontier cost] :as m}]
  (let [[next-state cst] (peek frontier)
        costs (->> next-state
                   neighbors
                   (map (fn [n] [n (+ cst (cost-fn next-state n))]))
                   (remove (fn [[n c]] (when-let [cc (cost n)] (>= c cc))))
                   (apply conj {}))]
    (-> m
        (update :frontier #(-> % pop (into costs)))
        (update :cost into costs)
        (update :visited into (zipmap (keys costs) (repeat next-state))))))

(defn dijkstra-seq [neighbors cost-fn start]
  (->> {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}}
       (iterate (partial dijkstra-step neighbors cost-fn))
       (take-while (comp seq :frontier))))

(defn dijkstra-path [neighbors cost-fn start goal]
  (->> (dijkstra-seq neighbors cost-fn start)
       (some (fn [{:keys [visited]}] (when (visited goal) visited)))
       (recover-path goal)))

(def height-map
  [[1 1 1 1 1 1]
   [1 1 2 2 2 1]
   [1 1 5 5 2 1]
   [1 1 5 5 2 1]
   [1 1 2 2 2 1]
   [1 1 1 1 1 1]])

(dijkstra-path
  (fn [a] (filter (partial get-in height-map) (neighbors a)))
  (fn [a b] (Math/abs (- (get-in height-map a) (get-in height-map b))))
  [0 0]
  [5 5])

(dijkstra-path neighbors-8 dist [0 0] [5 5])

;Next step is https://www.redblobgames.com/pathfinding/a-star/introduction.html#greedy-best-first

(prn (bfs [0 0] [5 5] neighbors))

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
