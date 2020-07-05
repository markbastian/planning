(ns planning.number-maze-test
  "Number Maze: Given a start and end integer and the ability to move from one
  value to the next with the operations +2, *2, and /2 (if even), what is the
  shortest set of numbers to get from start to end."
  (:require [clojure.test :refer :all]
            [planning.core :as p]))

(deftest simple-number-maze-test
  (testing "Demonstrate ability to provide arbitrary graph walk - no geometric grid required."
    (is (= [200 100 50 52 26 13 15 17]
           (p/breadth-first-search
             {:start        200
              :goal         17
              :neighbors-fn (fn [x] (cond-> [(* 2 x) (+ 2 x)] (even? x) (conj (/ x 2))))})))))

(deftest number-maze-with-operations-test
  (testing "Use metadata to recover the edge data as well."
    (is (= '({:value 200}
             {:value 100, :op /}
             {:value 50, :op /}
             {:value 52, :op +}
             {:value 26, :op /}
             {:value 13, :op /}
             {:value 15, :op +}
             {:value 17, :op +})
           (map
             (fn [m] (into m (meta m)))
             (p/breadth-first-search
               {:start        {:value 200}
                :goal         {:value 17}
                :neighbors-fn (fn [{:keys [value]}]
                                (cond-> [^{:op '*} {:value (* 2 value)}
                                         ^{:op '+} {:value (+ 2 value)}]
                                        (even? value)
                                        (conj ^{:op '/} {:value (/ value 2)})))}))))))