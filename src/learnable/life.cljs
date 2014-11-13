(ns learnable.life
  (:require [learnable.display :as display]))

(def neighbors (memoize
  (fn [cell population]
    (let [[x y] cell]
      (filter
        (fn [[cx cy]]
            (and
              (< (Math/abs (- cx x)) 2)
              (< (Math/abs (- cy y)) 2)))
        (disj population cell))))))

(defn should-live? [cell population]
  (let [ncount (count (neighbors cell population))]
    (or (= ncount 2) (= ncount 3))))

(defn should-spawn? [cell population]
  (let [ncount (count (neighbors cell population))]
    (= ncount 3)))

(def surrounding (memoize
  (fn [cell]
    (let [[x y] cell
          next-x (inc x)
          prev-x (dec x)
          next-y (inc y)
          prev-y (dec y)]
      (list (list next-x next-y)
            (list next-x prev-y)
            (list prev-x next-y)
            (list prev-x prev-y)
            (list x next-y)
            (list x prev-y)
            (list next-x y)
            (list prev-x y))))))

(defn get-potential [population]
  (distinct (concat population (mapcat surrounding population))))

(defn step [world]
  (let [population (:cells world)]
    (reduce
      (fn [generation cell]
        (if (contains? population cell)
          (if (should-live? cell population)
            generation
            (disj generation cell))
          (if (should-spawn? cell population)
            (conj generation cell)
            generation)))
      population
      (get-potential population))))

(def life-game
  {:boot
     (fn [screen]
       {:cells (set `())
        :status :paused})

   :draw
     (fn [state screen]
       (reduce (fn [screen cell] (display/draw-pixel screen cell (if (= :paused (:status state)) :red :green)))
               screen
               (:cells state)))
   :transitions
      {:clock
        (fn [state _]
          (if (= :paused (:status state))
              state
              (assoc state :cells (step state))))
      :mouse
        (fn [state point]
          (update-in state [:cells]
            (fn [population]
              (if (contains? population point)
                (disj population point)
                (conj population point)))))
      :keyboard (fn [state ks]
                  (when (= ks :key-up)
                    (assoc state
                      :status (if (= :paused (:status state))
                                :running
                                :paused))))}})
