(ns planning.core
  (:require #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer [priority-map]]
               :cljs [tailrecursion.priority-map :refer [priority-map]]))
  #?(:clj (:import (clojure.lang PersistentQueue))))

(def empty-queue #?(:clj (PersistentQueue/EMPTY) :cljs #queue[]))

(defn- update-frontier [m additions]
  (update m :frontier #(-> % pop (into additions))))

(defn- update-visited [m from-state new-neighbors]
  (update m :visited into (zipmap new-neighbors (repeat from-state))))

(defn bd-search-step [{:keys [neighbors-fn frontier visited] :as m}]
  (let [current-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors-fn current-state))]
    (-> m
        (update-frontier new-neighbors)
        (update-visited current-state new-neighbors))))

(defn greedy-breadth-first-step [{:keys [neighbors-fn heuristic-fn goal frontier visited] :as m}]
  (let [[current-state] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors-fn current-state))
        estimates (for [n new-neighbors] [n (heuristic-fn goal n)])]
    (-> m
        (update-frontier estimates)
        (update-visited current-state new-neighbors))))

(defn- neighbors-with-costs [current-state neighbors-fn current-costs cost-fn]
  (for [neighbor (neighbors-fn current-state)
        :let [new-cost (+ (current-costs current-state) (cost-fn current-state neighbor))
              old-cost (current-costs neighbor ##Inf)]
        :when (< new-cost old-cost)]
    [neighbor new-cost]))

(defn dijkstra-step [{:keys [neighbors-fn cost-fn frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors-fn cost cost-fn)]
    (-> m
        (update-frontier costs)
        (update :cost into costs)
        (update-visited current-state (map first costs)))))

(defn A-star-step [{:keys [neighbors-fn cost-fn heuristic-fn goal frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors-fn cost cost-fn)
        estimates (map (fn [[s c]] [s (+ c (heuristic-fn goal s))]) costs)]
    (-> m
        (update-frontier estimates)
        (update :cost into costs)
        (update-visited current-state (map first costs)))))

(defn- initialize [{:keys [q cost-fn start] :as m}]
  (cond->
    (assoc m
      :visited {start nil}
      :frontier (if q (conj q start) (priority-map start 0)))
    cost-fn (assoc :cost {start 0})))

(defn- search-seq [algorithm-step-fn]
  (fn [initial-conditions]
    (->> (initialize initial-conditions)
         (iterate algorithm-step-fn)
         (take-while (comp seq :frontier)))))

(def breadth-first-seq (comp (search-seq bd-search-step) #(assoc % :q empty-queue)))
(def depth-first-seq (comp (search-seq bd-search-step) #(assoc % :q [])))
(def dijkstra-seq (search-seq dijkstra-step))
(def greedy-breadth-first-seq (search-seq greedy-breadth-first-step))
(def A-star-seq (search-seq A-star-step))

(defn find-goal-state [search-seq]
  (first (filter (fn [{:keys [goal visited]}] (visited goal)) search-seq)))

(def breadth-first-terminus (comp find-goal-state breadth-first-seq))
(def depth-first-terminus (comp find-goal-state depth-first-seq))
(def dijkstra-terminus (comp find-goal-state dijkstra-seq))
(def greedy-breadth-first-terminus (comp find-goal-state greedy-breadth-first-seq))
(def A-star-terminus (comp find-goal-state A-star-seq))

(defn recover-path [{:keys [goal visited]}]
  (when-some [visited-goal (-> visited keys set (get goal))]
    (vec (reverse (take-while some? (iterate visited visited-goal))))))

(def breadth-first-search (comp recover-path breadth-first-terminus))
(def depth-first-search (comp recover-path depth-first-terminus))
(def dijkstra-search (comp recover-path dijkstra-terminus))
(def greedy-breadth-first-search (comp recover-path greedy-breadth-first-terminus))
(def A-star-search (comp recover-path A-star-terminus))