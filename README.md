# Planning [![Clojars Project](https://img.shields.io/clojars/v/markbastian/planning.svg)](https://clojars.org/markbastian/planning)

An implementation of planning algorithms in Clojure.

Currently provided algorithms are:

 * [Breadth first search](https://en.wikipedia.org/wiki/Breadth-first_search)
 * [Depth first search](https://en.wikipedia.org/wiki/Depth-first_search)
 * [Greedy breadth first search](https://en.wikipedia.org/wiki/Best-first_search)
 * [Dijksta's algorithm](https://en.wikipedia.org/wiki/Dijkstra%27s_algorithm)
 * [A*](https://en.wikipedia.org/wiki/A*_search_algorithm)

All algorithms are specified by an input map of the form:
```clojure
{:start        start-state ;Required for all
 :goal         goal-state ;Required for search functions (where a path is provided)
 :neighbors-fn neighbors-fn ;Required for all. Takes a state and returns a sequence of neighboring states immediately reachable by the provided state.
 :heuristic-fn heuristic-fn ;Required for greedy BFS & A*. Takes two states and returns the estimated numeric cost of going from one to the other.
 :cost-fn      cost-fn ;Required for Dijkstra & A*. Takes two states and returns the actual numeric cost of going from one to the other.
}
```

Three families of functions are provided for each algorithm:
 * seq functions (e.g. `A-star-seq`) that return a lazy seq of solution steps. Will terminate for finite search spaces when all nodes have been visited. No `:goal` is required for seq functions.
 * terminus functions (e.g. `A-star-terminus`) that provide the complete final state of the algorithm when the goal is found.
 * search functions (e.g. `A-star-search`) that return the solved path from start to goal (or nil if none).

## Examples
The test directory is a great place to find examples of how to use these algorithms.

Using the planning algorithms is quite simple.

```clojure
;;Pull in the core dependency
(require '[planning.core :as p])

;;Compute the steps required to go from 200 to 17 by only 
;; * doubling
;; * adding 2
;; * or halving the input (if even) at each step.
;;Note that the neighbors function is an unbounded search space 
;; (e.g. as opposed to a simple grid).
(p/breadth-first-search
 {:start        200
  :goal         17
  :neighbors-fn (fn [x] (cond-> [(* 2 x) (+ 2 x)] (even? x) (conj (/ x 2))))})

;;Pull in the utility functions
(require '[planning.utils :as u])

;;You might have a heightmap defined (maybe for a game)
(def height-map
  [[1 1 1 1 2 1]
   [1 1 2 2 2 1]
   [1 1 5 5 2 1]
   [1 1 5 5 2 1]
   [1 2 2 2 2 1]
   [1 1 1 1 1 1]])

;;Not all algorithms need all keys, but we'll put them all there so we can try 
;;them all out.
(def setup
  {:start        [0 0]
   :goal         [5 5]
   :neighbors-fn (partial u/moore-neigbors height-map)
   :heuristic-fn u/euclidian-distance
   :cost-fn      (partial u/heightmap-distance height-map)})

(u/mark-path height-map (p/breadth-first-search setup))
(u/mark-path height-map (p/depth-first-search setup))
(u/mark-path height-map (p/dijkstra-search setup))
(u/mark-path height-map (p/greedy-breadth-first-search setup))
(u/mark-path height-map (p/A-star-search setup))

;;To view the state of any algorithm, you can use the seq fn. For example:
(->> (p/breadth-first-seq setup)
     (take 3))

(->> (p/depth-first-seq setup)
     (take 3))

(->> (p/A-star-seq setup)
     (take 3))

;;Inspect how a particular algorithm traverses the domain
(->> (p/A-star-seq setup)
     (map (comp ffirst :frontier)))
```

## About

Great documentation on these algorithms can be found at:

  * [Steven Lavalle's Planning Algorithms Book](http://planning.cs.uiuc.edu)
  * [Red Blob Games](https://www.redblobgames.com)

## License

Copyright © 2015 Mark S. Bastian

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
