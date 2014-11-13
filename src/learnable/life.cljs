(ns learnable.life
  (:require [learnable.display :as display]))

(defn neighbors [cell population]
    (let [[x y] cell]
      (filter
        (fn [[cx cy]]
            (and
              (< (Math/abs (- cx x)) 2)
              (< (Math/abs (- cy y)) 2)))
        (disj population cell))))

(defn should-live? [cell population]
  (let [ncount (count (neighbors cell population))]
    (or (= ncount 2) (= ncount 3))))

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

(defn step [state]
  (let [population (:cells state)]
    (persistent!
      (reduce
        (fn [generation spawned]
          (conj! generation (first spawned)))
        (reduce
          (fn [survivors cell]
            (if (should-live? cell population)
              survivors
              (disj! survivors cell)))
          (transient population)
          population)
        (filter
          (fn [[_ cnt]]
            (= cnt 3))
          (frequencies (mapcat surrounding population)))))))

(def life-game
  {:boot
     (fn [screen]
       {:cells (set `((16 13) (16 11) (14 12) (12 15) (16 12) (14 13) (10 16) (14 14) (12 16) (17 12)))
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
              (if (= :running (:status state))
                population
                (if (contains? population point)
                  (disj population point)
                  (conj population point))))))
      :keyboard (fn [state ks]
                  (when (= ks :key-up)
                    (assoc state
                      :status (if (= :paused (:status state))
                                :running
                                :paused))))}})
