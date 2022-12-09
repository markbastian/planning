(ns planning.towers-of-hanoi-test
  (:require [clojure.test :refer [deftest is testing]]
            [planning.core :as p]))

(defn move-to [s fi ti]
  (let [a (s fi)
        ta (peek a)
        b (s ti)
        tb (peek b)]
    (when (and ta (or (empty? b) (< ta tb)))
      (-> s
          (update fi pop)
          (update ti conj ta)))))

(defn neighbors [ic]
  (letfn [(add-neighbor [acc [fi ti]]
            (if-some [n (move-to ic fi ti)] (conj acc n) acc))]
    (reduce add-neighbor [] [[0 1] [0 2] [1 0] [1 2] [2 0] [2 1]])))

(deftest test-towers-of-hanoi
  (testing "Solve towers of Hanoi problem using DFS"
    (let [start-stack [3 2 1]
          start [start-stack [] []]
          goal (reverse start)
          solution (p/breadth-first-search
                     {:start        start
                      :goal         goal
                      :neighbors-fn neighbors})]
      (is (= (peek solution) goal))
      (is (= (bit-shift-left 1 (count start-stack)) (count solution))))))