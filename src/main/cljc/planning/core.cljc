(ns planning.core
  (:require #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer [priority-map]]
               :cljs [tailrecursion.priority-map :refer [priority-map]]))
  #?(:clj (:import (clojure.lang PersistentQueue))))

(def empty-queue #?(:clj (PersistentQueue/EMPTY) :cljs #queue[]))

(defn update-frontier [m additions]
  (update m :frontier #(-> % pop (into additions))))

(defn update-visited [m from-state new-neighbors]
  (update m :visited into (zipmap new-neighbors (repeat from-state))))

(defn bd-search-step [{:keys [neighbors frontier visited] :as m}]
  (let [current-state (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))]
    (-> m
        (update-frontier new-neighbors)
        (update-visited current-state new-neighbors))))

(defn greedy-breadth-first-step [{:keys [neighbors heuristic-fn goal frontier visited] :as m}]
  (let [[current-state] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))
        estimates (for [n new-neighbors] [n (heuristic-fn goal n)])]
    (-> m
        (update-frontier estimates)
        (update-visited current-state new-neighbors))))

(defn- neighbors-with-costs [current-state neighbors-fn current-costs cost-fn]
  (for [neighbor (neighbors-fn current-state)
        :let [new-cost (+ (current-costs current-state) (cost-fn current-state neighbor))]
        :when (< new-cost (current-costs neighbor ##Inf))]
    [neighbor new-cost]))

(defn dijkstra-step [{:keys [neighbors cost-fn frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors cost cost-fn)]
    (-> m
        (update-frontier costs)
        (update :cost into costs)
        (update-visited current-state (map first costs)))))

(defn A-star-step [{:keys [neighbors cost-fn heuristic-fn goal frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (neighbors-with-costs current-state neighbors cost cost-fn)
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

(comment
  (breadth-first-search
    {:start     200
     :goal      17
     :neighbors (fn [x] (cond-> [(* 2 x) (+ 2 x)] (even? x) (conj (/ x 2))))})

  (map
    (fn [m] (into m (meta m)))
    (breadth-first-search
      {:start     {:value 200}
       :goal      {:value 17}
       :neighbors (fn [{:keys [value]}]
                    (cond-> [^{:op '*} {:value (* 2 value)}
                             ^{:op '+} {:value (+ 2 value)}]
                            (even? value)
                            (conj ^{:op '/} {:value (/ value 2)})))}))

  (require '[planning.utils :as u])
  (def grid [[1 1 1 1 2 1]
             [1 1 2 2 2 1]
             [1 1 5 5 2 1]
             [1 1 5 5 2 1]
             [1 2 2 2 2 1]
             [1 1 1 1 1 1]])

  (letfn [(dist [a b]
            (let [u (conj a (get-in grid a))
                  v (conj b (get-in grid b))
                  w (map - u v)]
              (Math/sqrt (reduce + (map * w w)))))]
    (let [path (dijkstra-search
                 {:start     [0 0]
                  :goal      [5 5]
                  :neighbors (partial u/moore-neigbors grid)
                  :cost-fn   dist})]
      (u/mark-path grid path)))

  (letfn [(dist [a b]
            (let [u (conj a (get-in grid a))
                  v (conj b (get-in grid b))
                  w (map - u v)]
              (Math/sqrt (reduce + (map * w w)))))]
    (let [path (A-star-search
                 {:start        [0 0]
                  :goal         [5 5]
                  :neighbors    (partial u/moore-neigbors grid)
                  :cost-fn      dist
                  :heuristic-fn dist})]
      (u/mark-path grid path)))

  (let [path
        (depth-first-search
          {:start     [0 0]
           :goal      [5 5]
           :neighbors (partial u/moore-neigbors grid)})]
    (u/mark-path grid path))

  (let [path
        (breadth-first-search
          {:start     [0 0]
           :goal      [5 5]
           :neighbors (partial u/moore-neigbors grid)})]
    (u/mark-path grid path)))