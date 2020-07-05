(ns planning.grid-search-test
  (:require [clojure.test :refer :all]
            [planning.utils :as u]
            [planning.core :as p]
            [clojure.pprint :as pp]))

(defn normalize-grid
  "Replace 0s with nil, making them unpassable."
  [grid]
  (mapv (fn [row] (mapv (fn [v] (get {0 nil} v v)) row)) grid))

(defn setup-grid-search
  "Create a default grid search setup, with grid cells in [0, 9] where 0 is not
  passable and 1-9 are passable and higher is more difficult."
  [initial-grid start goal]
  (let [grid (normalize-grid initial-grid)]
    {:start        start
     :goal         goal
     :neighbors-fn (partial u/moore-neigbors grid)
     :heuristic-fn (partial u/heightmap-distance grid)
     :cost-fn      (partial u/heightmap-distance grid)}))

(def l-block-grid [[1 1 1 1 2 1]
                   [5 1 1 1 0 1]
                   [2 3 1 1 0 9]
                   [1 1 1 1 0 1]
                   [5 0 0 0 0 1]
                   [1 1 1 1 1 1]])

(def corner-to-corner-l-block-setup (setup-grid-search l-block-grid [0 0] [5 5]))

(deftest corner-to-corner-l-block-depth-first-search-tests
  (testing "Example-based corner-to-corner DFS solution with L block."
    (let [solution (p/depth-first-search corner-to-corner-l-block-setup)]
      (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 5] [2 5] [3 5] [4 5] [5 5]] solution))
      (is (= '[[X X X X X 1]
               [5 1 1 1 0 X]
               [2 3 1 1 0 X]
               [1 1 1 1 0 X]
               [5 0 0 0 0 X]
               [1 1 1 1 1 X]])
          (u/mark-path l-block-grid solution)))))

(deftest corner-to-corner-l-block-breadth-first-search-tests
  (testing "Example-based corner-to-corner BFS solution with L block."
    (let [solution (p/breadth-first-search corner-to-corner-l-block-setup)]
      (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [X 1 1 1 0 1]
               [X 3 1 1 0 9]
               [X 1 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]])
          (u/mark-path l-block-grid solution)))))

(deftest corner-to-corner-l-block-greedy-breadth-first-search-search-tests
  (testing "Example-based corner-to-corner Greedy BFS solution with L block."
    (let [solution (p/greedy-breadth-first-search corner-to-corner-l-block-setup)]
      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [2 3 X 1 0 9]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]])
          (u/mark-path l-block-grid solution)))))

(deftest corner-to-corner-l-block-dijkstra-search-search-tests
  (testing "Example-based corner-to-corner Dijkstra solution with L block."
    (let [solution (p/dijkstra-search corner-to-corner-l-block-setup)]
      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [2 3 X 1 0 9]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]])
          (u/mark-path l-block-grid solution)))))

(deftest corner-to-corner-l-block-A-star-search-search-tests
  (testing "Example-based corner-to-corner A* solution with L block."
    (let [solution (p/A-star-search corner-to-corner-l-block-setup)]
      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 1] [5 2] [5 3] [5 4] [5 5]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [2 3 X 1 0 9]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [1 X X X X X]])
          (u/mark-path l-block-grid solution)))))

(def vertical-l-block-setup (setup-grid-search l-block-grid [0 0] [5 0]))

(deftest vertical-l-block-depth-first-search-tests
  (testing "Example-based vertical DFS solution with L block."
    (let [solution (p/depth-first-search vertical-l-block-setup)]
      (is (= [[0 0] [0 1] [0 2] [0 3] [0 4] [1 5] [2 5] [3 5] [4 5] [5 4] [5 3] [5 2] [5 1] [5 0]] solution))
      (is (= '[[X X X X X 1]
               [5 1 1 1 0 X]
               [2 3 1 1 0 X]
               [1 1 1 1 0 X]
               [5 0 0 0 0 X]
               [X X X X X 1]])
          (u/mark-path l-block-grid solution)))))

(deftest vertical-l-block-breadth-first-search-tests
  (testing "Example-based vertical BFS solution with L block."
    (let [solution (p/breadth-first-search vertical-l-block-setup)]
      (is (= [[0 0] [1 0] [2 0] [3 0] [4 0] [5 0]] solution))
      (is (= '[[X 1 1 1 2 1]
               [X 1 1 1 0 1]
               [X 3 1 1 0 9]
               [X 1 1 1 0 1]
               [X 0 0 0 0 1]
               [X 1 1 1 1 1]])
          (u/mark-path l-block-grid solution)))))

(deftest vertical-l-block-greedy-breadth-first-search-search-tests
  (testing "Example-based vertical Greedy BFS solution with L block."
    (let [solution (p/greedy-breadth-first-search vertical-l-block-setup)]
      (is (= [[0 0] [1 1] [2 0] [3 0] [4 0] [5 0]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [X 3 1 1 0 9]
               [X 1 1 1 0 1]
               [X 0 0 0 0 1]
               [X 1 1 1 1 1]])
          (u/mark-path l-block-grid solution)))))

(deftest vertical-l-block-dijkstra-search-search-tests
  (testing "Example-based vertical Dijkstra solution with L block."
    (let [solution (p/dijkstra-search vertical-l-block-setup)]

      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 0]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [2 3 X 1 0 9]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [X 1 1 1 1 1]])
          (u/mark-path l-block-grid solution)))))

(deftest vertical-l-block-A-star-search-search-tests
  (testing "Example-based vertical A* solution with L block."
    (let [solution (p/A-star-search vertical-l-block-setup)]
      (is (= [[0 0] [1 1] [2 2] [3 1] [4 0] [5 0]] solution))
      (is (= '[[X 1 1 1 2 1]
               [5 X 1 1 0 1]
               [2 3 X 1 0 9]
               [1 X 1 1 0 1]
               [X 0 0 0 0 1]
               [X 1 1 1 1 1]])
          (u/mark-path l-block-grid solution)))))