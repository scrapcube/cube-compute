(ns learnable.snake
  (:require [learnable.display :as display]))

(defn translate [point ray]
  (let [[x y] point
        [dx dy] ray]
    [(+ x dx) (+ y dy)]))

(defn draw [state screen]
  (let [{:keys [food snake]} state]
    (reduce (fn [screen food-p] (display/draw-pixel screen food-p :red))
            (reduce (fn [screen snake-p] (display/draw-pixel screen snake-p :green))
                    screen
                    snake)
            food)))

(defn out-of-bounds? [[x y] width height]
  (or (or (< x 0)
          (>= x width))
      (or (< y 0)
          (>= y height))))

(defn on-clock [state _]
  (let [{:keys [world-w
                world-h
                food
                snake
                energy
                direction
                status]} state
        head (last snake)
        front (translate head direction)
        is-front? (fn [x] (= front x))]
    (cond (some is-front? food)
            (assoc state
                   :snake (concat (rest snake) (list front))
                   :energy (inc energy)
                   :food (remove is-front? food))
          (or (some is-front? snake)
              (out-of-bounds? front world-w world-h))
            (assoc state :status :dead)
          :else
            (if (= energy 0)
              (assoc state :snake (concat (rest snake)
                                         (list front)))
              (assoc state :snake (concat snake
                                         (list front))
                           :energy (dec energy))))))

(defn to-direction [keystroke]
  (condp = keystroke
         :key-up [0 -1]
         :key-right [1 0]
         :key-down [0 1]
         :key-left [-1 0]
         nil))

(defn on-keyboard [state keystroke]
  (let [{:keys [snake direction]} state]
    (update-in state
               [:direction]
               (fn [direction]
                 (let [head (last snake)
                       neck (last (butlast snake))
                       new-direction (to-direction keystroke)]
                   (if (or (= nil new-direction)
                           (= neck (translate head new-direction)))
                     direction
                     new-direction))))))

(defn is-over-snake? [state]
  (= (:status state) :dead))

(def snake-program
  {:boot
    (fn [screen]
      {:world-w (first (:resolution screen))
       :world-h (last (:resolution screen))
       :snake [[2 2]]
       :food [[4 1] [2 0] [4 7] [6 3]]
       :energy 0
       :direction [0 1]
       :status :alive})
   :draw
      draw
   :transitions
      {:clock on-clock
       :keyboard on-keyboard}})

