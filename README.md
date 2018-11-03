# Planning

An implementation of planning algorithms in Clojure.

## About

Great documentation on these algorithms can be found at:
  * [Steven Lavalle's Planning Algorithms Book](http://planning.cs.uiuc.edu)
  * [Red Blob Games](https://www.redblobgames.com)

## Usage

Run using lein run or build using lein uberjar.

```clojure
(priority-map :a 4 :b 1 :c 2)
(prn (bfs [0 0] [5 5] neighbors))
(prn (dijkstra [0 0] [5 5] neighbors-with-cost))
```

## License

Copyright © 2015 Mark S. Bastian

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
