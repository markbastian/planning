(ns planning.core
  (:require #?(:clj [clojure.pprint :refer [pprint]]
               :cljs [cljs.pprint :refer [pprint]])
    #?(:clj [clojure.data.priority-map :refer
             [priority-map priority-map-by]]
       :cljs [tailrecursion.priority-map :refer
             [priority-map priority-map-by]])))

(def empty-queue #?(:clj (clojure.lang.PersistentQueue/EMPTY)
                    :cljs (cljs.core.PersistentQueue/EMPTY)))

;;See page 29 of Lavalle - stf = state transition function. Also, just "neighbors"
(defn search [q initial-state goal stf]
  (loop [Q (conj q initial-state) visited { initial-state nil }]
    (cond
      (= (peek Q) goal) (vec (take-while some? (iterate visited goal)))
      (empty? Q) nil
      :else (let [neighbors (remove #(contains? visited %) (stf (peek Q)))]
              (recur
                (into (pop Q) neighbors)
                (into visited (zipmap neighbors (repeat (peek Q)))))))))

(def dfs (partial search []))
(def bfs (partial search empty-queue))

(defn dijkstra "Compute path from initial-state to goal using stf. Note that stf
returns a map of states to costs."
  [initial-state goal stf]
  (loop [Q (conj [] initial-state)
         visited { initial-state nil }
         costs { initial-state 0 }]
    (cond
      (= (peek Q) goal) (vec (take-while some? (iterate visited goal)))
      (empty? Q) nil
      :else (let [neighbors (stf (peek Q))
                  nx (zipmap (keys neighbors)
                             (map + (repeat (costs (peek Q))) (vals neighbors)))
                  x (for [[n c] nx :when (> (costs n c) c)] [n c])]
              (recur
                (into (pop Q) (keys x))
                (into visited (zipmap (keys x) (repeat (peek Q))))
                (into costs x))))))

(defn neighbors [[x y]]
  (let [i ((juxt inc identity dec identity) x)
        j ((juxt identity inc identity dec) y)]
    (map vector i j)))

(defn neighbors-with-cost [[x y]]
  (let [i ((juxt inc identity dec identity) x)
        j ((juxt identity inc identity dec) y)]
    (into {} (map (fn [i j]
                  (let [dst [i j]
                        v (map - dst [x y])
                        m (Math/sqrt (reduce + (map * v v)))]
                    {dst m})) i j))))

;(priority-map :a 4 :b 1 :c 2)
;(prn (bfs [0 0] [5 5] neighbors))
;(prn (dijkstra [0 0] [5 5] neighbors-with-cost))