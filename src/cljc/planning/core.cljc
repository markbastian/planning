(ns planning.core
  (:require #?(:clj  [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
            #?(:clj  [clojure.data.priority-map :refer [priority-map]]
               :cljs [tailrecursion.priority-map :refer [priority-map]])))

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

(def infinity #?(:clj Double/POSITIVE_INFINITY, :cljs Number/POSITIVE_INFINITY))

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

;;Helper functions
(defn exhaust-search [search-seq]
  (take-while (comp seq :frontier) search-seq))

(defn goal-met [goal goal-steps]
  (some (fn [{:keys [visited]}] (when (visited goal) visited)) goal-steps))

(defn recover-path [goal visited]
  (vec (reverse (take-while some? (iterate visited goal)))))

(defn search [goal search-seq]
  (some->> search-seq (goal-met goal) (recover-path goal)))

;Breadth first search
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
        :when (< new-cost (current-costs neighbor infinity))]
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
(defn greedy-bfs-step [neighbors heuristic {:keys [goal frontier visited] :as m}]
  (let [[current-state _] (peek frontier)
        new-neighbors (remove #(contains? visited %) (neighbors current-state))]
    (-> m
        (update :frontier #(-> % pop (into (for [n new-neighbors] [n (heuristic goal n)]))))
        (update :visited into (zipmap new-neighbors (repeat current-state))))))

(defn greedy-bfs-seq [neighbors heuristic {:keys [start] :as m}]
  (->> m
       (into {:frontier (priority-map start 0) :visited {start nil}})
       (iterate (partial greedy-bfs-step neighbors heuristic))
       exhaust-search))

(defn greedy-bfs-search [neighbors heuristic {:keys [goal] :as m}]
  (search goal (greedy-bfs-seq neighbors heuristic m)))

;A* algorithm
(defn A*-step [neighbors cost-fn heuristic {:keys [goal frontier cost] :as m}]
  (let [[current-state] (peek frontier)
        costs (compute-costs current-state neighbors cost cost-fn)
        estimates (map (fn [[s c]] [s (+ c (heuristic goal s))]) costs)]
    (-> m
        (update :frontier #(-> % pop (into estimates)))
        (update :cost into costs)
        (update :visited into (map (fn [[c]] [c current-state]) costs)))))

(defn A*-seq [neighbors cost-fn heuristic {:keys [start] :as m}]
  (->> m
       (into {:frontier (priority-map start 0) :cost {start 0} :visited {start nil}})
       (iterate (partial A*-step neighbors cost-fn heuristic))
       exhaust-search))

(defn A*-search [neighbors cost-fn heuristic {:keys [goal] :as m}]
  (search goal (A*-seq neighbors cost-fn heuristic m)))

(defmacro defsearch [search-name n & args]
  `(defn ~search-name [~@args]
     (search (:goal ~(last args)) (~n ~@args))))

(macroexpand
  '(defsearch A* A*-seq neighbors cost-fn heuristic m))

(defsearch A* A*-seq neighbors cost-fn heuristic m)
