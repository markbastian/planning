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

(deftest test-dijkstra-path-with-height-delta-cost
  (testing "Dijkstra's algorithm with a cost function of absolute difference in cell heights."
    (let [solution (p/dijkstra-path
                     (fn [a] (filter (partial get-in p/height-map) (p/neighbors a)))
                     (fn [a b] (inc (Math/abs (double (- (get-in height-map a) (get-in height-map b))))))
                     [0 0]
                     [5 5])]
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
                     (fn [a] (filter (partial get-in p/height-map) (p/neighbors-8 a)))
                     p/euclidian-distance
                     [0 0]
                     [5 5])]
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
                     (fn [a] (filter (partial get-in p/height-map) (p/neighbors-8 a)))
                     p/euclidian-distance
                     [0 0]
                     [5 5])]
      (is (= [[0 0] [1 1] [2 2] [3 3] [4 4] [5 5]] solution)))))

