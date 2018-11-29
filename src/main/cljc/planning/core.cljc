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

(defn goal-met [search-seq]
  (some (fn [{:keys [goal visited] :as m}] (when (visited goal) m)) search-seq))

(defn recover-path [{:keys [goal visited]}]
  (vec (reverse (take-while some? (iterate visited goal)))))

(defn path-search [search-seq]
  (some->> search-seq goal-met recover-path))

(defn prep [{:keys [q cost-fn start] :as m}]
  (cond->
    (assoc m
      :visited {start nil}
      :frontier (if q (conj q start) (priority-map start 0)))
    cost-fn (assoc :cost {start 0})))

(defn search-seq [algorithm-step-fn] #(->> % prep (iterate algorithm-step-fn) exhaust-search))

(defn general-search [algorithm-step-fn] (comp path-search (search-seq algorithm-step-fn)))

(defn- neighbors-with-costs [current-state neighbors-fn current-costs cost-fn]
  (for [neighbor (neighbors-fn current-state)
        :let [new-cost (+ (current-costs current-state) (cost-fn current-state neighbor))]
        :when (< new-cost (current-costs neighbor ##Inf))]
    [neighbor new-cost]))

(defn update-frontier [m additions]
  (update m :frontier #(-> % pop (into additions))))

(defn update-visited [m from-state new-neighbors]
  (update m :visited into (zipmap new-neighbors (repeat from-state))))

(defn bd-search-step [{:keys [neighbors frontier visited] :as m}]
  (let [next-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors next-state))]
    (-> m
        (update-frontier new-neighbors)
        (update-visited next-state new-neighbors))))

(defn dijkstra-step [{:keys [neighbors cost-fn frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors cost cost-fn)]
    (-> m
        (update-frontier costs)
        (update :cost into costs)
        (update-visited current-state (map first costs)))))

(defn greedy-bfs-step [{:keys [neighbors heuristic-fn goal frontier visited] :as m}]
  (let [[current-state] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))
        estimates (for [n new-neighbors] [n (heuristic-fn goal n)])]
    (-> m
        (update-frontier estimates)
        (update-visited current-state new-neighbors))))

(defn A*-step [{:keys [neighbors cost-fn heuristic-fn goal frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors cost cost-fn)
        estimates (map (fn [[s c]] [s (+ c (heuristic-fn goal s))]) costs)]
    (-> m
        (update-frontier estimates)
        (update :cost into costs)
        (update-visited current-state (map first costs)))))

(def bd-search-seq (search-seq bd-search-step))
(def dijkstra-seq (search-seq dijkstra-step))
(def greedy-bfs-seq (search-seq greedy-bfs-step))
(def A*-seq (search-seq A*-step))

(def iterative-search (general-search bd-search-step))
(def breadth-first-search (comp iterative-search #(assoc % :q empty-queue)))
(def depth-first-search (comp iterative-search #(assoc % :q [])))

(def dijkstra-path (general-search dijkstra-step))
(def greedy-bfs-search (general-search greedy-bfs-step))
(def A*-search (general-search A*-step))