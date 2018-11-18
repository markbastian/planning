(ns planning.app
  (:require [planning.core :as p]
            [planning.utils :as u]
            [tailrecursion.priority-map :refer [priority-map]]
            [reagent.core :as reagent :refer [atom cursor]]))

(def people '{:elf 🧝})

(def items '{:dagger 🗡️ :swords ⚔ :key 🗝️️ :crown 👑})

(def cost '{🌲 1 🌳 1.5 🌴 2 ⛰ 5 🌋 10})

(def grid '[[🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 ⛰ ⛰ ⛰ ⛰ ⛰ 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 ⛰ ⛰ ⛰ ⛰ ⛰ 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 ⛰ ⛰ ⛰ ⛰ ⛰ 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 ⛰ ⛰ ⛰ ⛰ ⛰ 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]
            [🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲 🌲]])

(def state (atom {:grid (apply mapv vector grid)}))

(defn A*-meadow-search [{:keys [grid start goal] :as m}]
  (when (and start goal)
    (p/A*-search
      (partial u/moore-neigbors grid)
      (fn [f t] (* (cost (get-in grid t) ##Inf) (u/euclidian-distance f t)))
      u/euclidian-distance
      m)))

(defn render-path [state]
  (let [cell-dim 16]
    (doall
      (for [[i j] (A*-meadow-search @state)]
        [:circle {:key    (str "step:" i ":" j)
                  :cx     (* (+ i 0.5) cell-dim)
                  :cy     (* (+ j 0.5) cell-dim)
                  :r      (* cell-dim 0.5)
                  :stroke :black
                  :fill   :green}]))))

(defn render-goal [state state-key emoji]
  (let [goal (cursor state [state-key])
        cell-dim 16]
    (fn []
      (let [[i j] @goal]
        (when @goal
          [:text {:x       (* i cell-dim)
                  :y       (* (inc j) cell-dim)
                  :onClick #(swap! state dissoc state-key)}
           emoji])))))

(defn add-paintbrush [state brush]
  (let [paintbrush (cursor state [:paintbrush])]
    (fn []
      [:span {:onClick #(if-not (= brush @paintbrush)
                          (reset! paintbrush brush)
                          (swap! state dissoc :paintbrush))
              :style   (cond-> {:cursor :pointer}
                               (= brush @paintbrush)
                               (assoc :text-decoration :underline))}
       brush])))

;https://developer.mozilla.org/en-US/docs/Web/SVG/Attribute/Events
;https://developer.mozilla.org/en-US/docs/Web/API/Touch_events
;https://reactjs.org/docs/events.html#touch-events
;https://www.javascripture.com/Touch
(defn render [state]
  (let [w 20 h 20 cell-dim 16
        grid (cursor state [:grid])
        paintbrush (cursor state [:paintbrush])
        dragging (atom false)]
    (fn []
      [:div {:style {:cursor :pointer :user-select :none}}
       [:svg {:width        (* cell-dim w)
              :height       (* cell-dim h)
              :onTouchStart #(do (prn "start") (reset! dragging true))
              :onTouchEnd   #(reset! dragging false)
              :onMouseDown  #(reset! dragging true)
              :onMouseUp    #(reset! dragging false)
              :onMouseLeave #(reset! dragging false)}
        [:rect {:width (* w cell-dim) :height (* h cell-dim) :fill :green}]
        (render-path state)
        (doall
          (for [i (range w) j (range h) :let [c (get-in @grid [i j])]]
            [:text {:key         (str i ":" j)
                    :x           (* i cell-dim)
                    :y           (* (inc j) cell-dim)
                    :data-i      i
                    :data-j      j
                    :onMouseOver #(when (and @dragging @paintbrush)
                                    (swap! grid assoc-in [i j] @paintbrush))
                    ;This is why we can't have nice things in JavaScript :( TouchEvent = UGH!!!
                    :onTouchMove (fn [event]
                                   (when (and @dragging @paintbrush)
                                     (let [touch (.item (.-touches event) 0)
                                           dataset (.-dataset (. js/document (elementFromPoint (.-clientX touch) (.-clientY touch))))
                                           i (some-> dataset .-i js/parseInt)
                                           j (some-> dataset .-j js/parseInt)]
                                       (when (and i j)
                                         (swap! grid assoc-in [i j] @paintbrush)))))
                    :onClick     #(cond
                                    @paintbrush (swap! grid assoc-in [i j] @paintbrush)
                                    (= [i j] (:start @state)) (swap! state dissoc :start)
                                    (= [i j] (:goal @state)) (swap! state dissoc :goal)
                                    (:start @state) (swap! state assoc :goal [i j])
                                    :default (swap! state assoc :start [i j]))}
             c]))
        [render-goal state :start '🧝]
        [render-goal state :goal '🗃️]]
       [:div
        [:span
         [add-paintbrush state '🌲]
         [add-paintbrush state '🌳]
         [add-paintbrush state '🌴]
         [add-paintbrush state '⛰]
         [add-paintbrush state '🌋]]]])))

(when-let [app-context (. js/document (getElementById "app"))]
  (let [state state]
    (reagent/render-component
      [render state]
      app-context)))