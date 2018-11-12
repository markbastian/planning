(ns planning.core-test
  (:require [clojure.test :refer :all]
            [planning.core :as p]))

(def height-map
  [[1 1 1 1 2 1]
   [1 1 2 2 2 1]
   [1 1 5 5 2 1]
   [1 1 5 5 2 1]
   [1 2 2 2 2 1]
   [1 1 1 1 1 1]])

(def barrier-map
  '[[1 1 1 1 1 1]
    [1 1 0 0 0 1]
    [1 1 1 1 0 1]
    [1 1 1 1 0 1]
    [1 0 0 0 0 1]
    [1 1 1 1 1 1]])

(deftest test-dijkstra-path-with-height-delta-cost
  (testing "Dijkstra's algorithm with a cost function of absolute difference in cell heights."
    (let [solution (p/dijkstra-path
                     (fn [a] (filter (partial get-in height-map) (p/neighbors a)))
                     (fn [a b] (inc (Math/abs (double (- (get-in height-map a) (get-in height-map b))))))
                     {:start [0 0] :goal [5 5]})]
      (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [X 1 2 2 2 1]
               [X 1 5 5 2 1]
               [X 1 5 5 2 1]
               [X 2 2 2 2 1]
               [X X X X X X]]
             (p/mark-path height-map solution))))))

(deftest test-dijkstra-path-with-diagonal-delta-cost
  (testing "Dijkstra's algorithm with a cost function that takes into account the added distance of diagonal moves."
    (let [solution (p/dijkstra-path
                     (fn [a] (filter (partial get-in height-map) (p/neighbors-8 a)))
                     p/euclidian-distance
                     {:start [0 0] :goal [5 5]})]
      (is (= [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [1 X 2 2 2 1]
               [1 1 X 5 2 1]
               [1 1 5 X 2 1]
               [1 2 2 2 X 1]
               [1 1 1 1 1 X]]
             (p/mark-path height-map solution))))))

(deftest test-greedy-bfs
  (testing "Greedy BFS with diagonal moves"
    (let [solution (p/greedy-bfs-search
                     (fn [a] (filter (partial get-in height-map) (p/neighbors-8 a)))
                     p/euclidian-distance
                     {:start [0 0] :goal [5 5]})]
      (is (= [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5]] solution)))))

(defn barrier-neighbors [c]
  (filter (fn [n] (when-some [h (get-in barrier-map n)] (pos? h))) (p/neighbors-8 c)))

(deftest dijkstra-with-barriers
  (testing "Dijkstra's algorithm will find the best path (not shown here, is the more exhaustive search performed)."
    (let [solution (p/dijkstra-path
                     barrier-neighbors
                     p/euclidian-distance
                     {:start [0 0] :goal [5 5]})]
      (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 1 1]
               [X 1 0 0 0 1]
               [X 1 1 1 0 1]
               [X 1 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]]
             (p/mark-path barrier-map solution))))))

(deftest greedy-bfs-with-barriers
  (testing "Greedy BFS will not find the best solution."
    (let [solution (p/greedy-bfs-search
                     barrier-neighbors
                     p/euclidian-distance
                     {:start [0 0] :goal [5 5]})]
      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 1 1]
               [1 X 0 0 0 1]
               [1 1 X 1 0 1]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]]
             (p/mark-path barrier-map solution))))))

