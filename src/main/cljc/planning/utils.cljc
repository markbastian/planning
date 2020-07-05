(ns planning.utils)

(defn grid-neigbors [neighbors grid c]
  (filter (partial get-in grid) (neighbors c)))

(defn von-neumann-neighbors
  "Return the 8 neighboring cells in a cartesian grid.
  https://en.wikipedia.org/wiki/Von_Neumann_neighborhood"
  ([[x y]]
   (let [i ((juxt inc identity dec identity) x)
         j ((juxt identity inc identity dec) y)]
     (map vector i j)))
  ([grid c] (grid-neigbors von-neumann-neighbors grid c)))

(defn moore-neigbors
  "Return the 4 neighboring adjacent cells in a cartesian grid.
  https://en.wikipedia.org/wiki/Moore_neighborhood"
  ([[x y]]
   (let [i ((juxt inc inc identity dec dec dec identity inc) x)
         j ((juxt identity inc inc inc identity dec dec dec) y)]
     (map vector i j)))
  ([grid c]
   (grid-neigbors moore-neigbors grid c)))

(defn euclidian-distance [a b]
  (let [u (map - a b)]
    (Math/sqrt (reduce + (map * u u)))))

(defn manhattan-distance [a b]
  (reduce + (map (comp #(Math/abs %) -) a b)))

(defn mark-path [grid solution]
  (reduce (fn [g c] (assoc-in g c 'X)) grid solution))
