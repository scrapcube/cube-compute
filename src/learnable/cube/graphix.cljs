(ns learnable.cube.graphix)
;;
;; Trees are at the heart of the graphics library.
;;
;; There are two types of entities, graphics and surfaces. These
;; correspond to leaf and non-leaf nodes of the graphics tree, respectively.

(def stypes (list :canvas :grid))

(defn is-surface? [obj]
  (some #(= % (:etype obj)) stypes))

(defn is-graphic? [obj]
  (not (is-surface? obj)))

;; Transforms
;; ==========

(defn offset-transform [offset]
  (fn [point]
    (map #(- %1 %2) point offset)))

(defn modulo-transform [offset resolution]
  (fn [point]
    (map #(Math/floor (/ %1 %2)) point resolution)))

;; Hash Map Constructors
;; =====================

(defn box [offset dimensions]
  {:offset offset :dimensions dimensions})

(defn entity [etype id]
  {:etype etype :id id :items []})

;; Surfaces
;; ========

(defmulti surface
  (fn [stype id offset dimensions & options]
    stype))

(defmethod surface :canvas [stype id offset dimensions]
  (merge (box offset dimensions)
         (entity stype id)
         {:transform (offset-transform offset)}))

(defmethod surface :grid [stype id offset dimensions resolution]
  (let [square-size (apply min (map #(/ %1 %2) dimensions resolution))
        adjusted-dimensions (map #(* % square-size) resolution)
        adjusted-offset
          (map #(+ %1 (/ (- %2 %3) 2))
            offset
            dimensions
            adjusted-dimensions)]
    (merge
      (box adjusted-offset adjusted-dimensions)
      (entity stype id)
        {:square-size square-size
         :resolution resolution
         :transform (comp (modulo-transform offset resolution)
                          (offset-transform offset))})))

(defn blit [dest-surface src-surface]
  (update-in dest-surface [:items] #(cons src-surface %)))

;; Graphics
;; ========

(defmulti draw
  (fn [surface gtype & params]
    [(:etype surface) gtype]))

(defmethod draw [:grid :pixel] [grid gtype point color]
  (let [{:keys [square-size]} grid]
    (blit grid
      (merge
        (box (map #(* square-size %) point)
             (list square-size square-size))
        (entity gtype (str point color))
        {:color color}))))
