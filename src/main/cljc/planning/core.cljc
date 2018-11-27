(ns planning.core
  (:require #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer [priority-map]]
               :cljs [tailrecursion.priority-map :refer [priority-map]]))
  #?(:clj (:import (clojure.lang PersistentQueue))))

(def empty-queue #?(:clj (PersistentQueue/EMPTY) :cljs #queue[]))

;;Helper functions
(defn exhaust-search [search-seq]
  (take-while (comp seq :frontier) search-seq))

(defn goal-met [goal goal-steps]
  (some (fn [{:keys [visited]}] (when (visited goal) visited)) goal-steps))

(defn recover-path [goal visited]
  (vec (reverse (take-while some? (iterate visited goal)))))

(defn search [goal search-seq]
  (some->> search-seq (goal-met goal) (recover-path goal)))

(defn search-step [neighbors {:keys [frontier visited] :as m}]
  (let [next-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors next-state))]
    (-> m
        (update :frontier #(-> % pop (into new-neighbors)))
        (update :visited into (zipmap new-neighbors (repeat next-state))))))

(defn search-seq [q neighbors {:keys [start] :as m}]
  (->> m
       (into {:frontier (conj q start) :visited {start nil}})
       (iterate (partial search-step neighbors))
       exhaust-search))

(defn iterative-search [q neighbors {:keys [goal] :as m}]
  (search goal (search-seq q neighbors m)))

(def breadth-first-search (partial iterative-search empty-queue))
(def depth-first-search (partial iterative-search []))

(defn compute-costs [current-state neighbors-fn current-costs cost-fn]
  (for [neighbor (neighbors-fn current-state)
        :let [new-cost (+ (current-costs current-state) (cost-fn current-state neighbor))]
        :when (< new-cost (current-costs neighbor ##Inf))]
    [neighbor new-cost]))

;Dijkstra's algorithm
(defn dijkstra-step [neighbors cost-fn {:keys [frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (compute-costs current-state neighbors cost cost-fn)]
    (-> m
        (update :frontier #(-> % pop (into costs)))
        (update :cost into costs)
        (update :visited into (map (fn [[c]] [c current-state]) costs)))))

(defn dijkstra-seq [neighbors cost-fn {:keys [start] :as m}]
  (->> m
       (into {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}})
       (iterate (partial dijkstra-step neighbors cost-fn))
       exhaust-search))

(defn dijkstra-path [neighbors cost-fn {:keys [goal] :as m}]
  (search goal (dijkstra-seq neighbors cost-fn m)))

;Greedy best first search
(defn greedy-bfs-step [neighbors heuristic-fn {:keys [goal frontier visited] :as m}]
  (let [[current-state _] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))]
    (-> m
        (update :frontier #(-> % pop (into (for [n new-neighbors] [n (heuristic-fn goal n)]))))
        (update :visited into (zipmap new-neighbors (repeat current-state))))))

(defn greedy-bfs-seq [neighbors heuristic-fn {:keys [start] :as m}]
  (->> m
       (into {:frontier (priority-map start 0) :visited {start nil}})
       (iterate (partial greedy-bfs-step neighbors heuristic-fn))
       exhaust-search))

(defn greedy-bfs-search [neighbors heuristic-fn {:keys [goal] :as m}]
  (search goal (greedy-bfs-seq neighbors heuristic-fn m)))

;A* algorithm
(defn A*-step [neighbors cost-fn heuristic-fn {:keys [goal frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (compute-costs current-state neighbors cost cost-fn)
        estimates (map (fn [[s c]] [s (+ c (heuristic-fn goal s))]) costs)]
    (-> m
        (update :frontier #(-> % pop (into estimates)))
        (update :cost into costs)
        (update :visited into (map (fn [[c]] [c current-state]) costs)))))

(defn A*-seq [neighbors cost-fn heuristic-fn {:keys [start] :as m}]
  (->> m
       (into {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}})
       (iterate (partial A*-step neighbors cost-fn heuristic-fn))
       exhaust-search))

(defn A*-search [neighbors cost-fn heuristic-fn {:keys [goal] :as m}]
  (search goal (A*-seq neighbors cost-fn heuristic-fn m)))