(ns planning.swingui
  (:require [planning.core :as p]
            [clojure.pprint :as pp])
  (:import (javax.swing JFrame SwingUtilities)
           (java.awt BorderLayout Component Color Graphics2D)
           (java.awt.event MouseAdapter)
           (java.awt.geom Path2D$Double)))

(defn neighbors [path [x y]]
  (let [barrier (into #{} path)]
    (for [i ((juxt inc identity dec identity) x)
          j ((juxt identity inc identity dec) y)
          :when (and
                  (<= 0 i 400)
                  (<= 0 j 400)
                  (not (barrier [i j])))]
      [i j])))

(defn bfs-path[path] (p/bfs [0 0] [300 300] (partial neighbors path)))

(defn painter [state]
  (proxy [MouseAdapter] []
    (mousePressed [event] (swap! state into {:down true :path [[(.getX event) (.getY event)]]}))
    (mouseReleased [event] (swap! state
                                  #(-> %
                                       (assoc :down false)
                                       (update :path conj [(.getX event) (.getY event)])
                                       ((fn [{:keys [path] :as m}]
                                          (assoc m :shortest-path (bfs-path path)))))))
    (mouseDragged [event]
      (when (:down @state)
        (swap! state update :path conj [(.getX event) (.getY event)])))))

(defn pts->shape [[[sx sy :as f] & r]]
  (reduce
    (fn [p [x y]] (doto p (.lineTo x y)))
    (cond-> (Path2D$Double.) f (doto (.moveTo sx sy)))
    r))

(defn panel [state]
  (let [p (proxy [Component] []
            (paint [^Graphics2D g]
              (let []
                (doto g
                  (.setPaint Color/BLACK)
                  (.draw (pts->shape (:path @state)))
                  (.setPaint Color/RED)
                  (.draw (pts->shape (:shortest-path @state)))
                  ))))]
    (add-watch
      state
      ::re-render
      (fn [_ _ {op :path} {np :path}]
        (when-not (= op np)
          (SwingUtilities/invokeLater #(.repaint p)))))
    p))

#_
(let [state (atom {})
      p (painter state)]
  (do
    (doto (JFrame. "Planning")
      (.setSize 800 600)
      (.setDefaultCloseOperation JFrame/DISPOSE_ON_CLOSE)
      (.add (doto (panel state)
              (.addMouseListener p)
              (.addMouseMotionListener p)) BorderLayout/CENTER)
      (.setVisible true))))