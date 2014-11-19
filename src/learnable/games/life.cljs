(ns learnable.games.life
  (:require [learnable.cube.graphix :as graphix]))

(defn normalize [n m]
  (if (>= n 0) (mod n m) (mod (+ n m) m)))

(defn neighbors [cell state]
  (let [[x y] cell
        next-x (normalize (inc x) (:width state))
        prev-x (normalize (dec x) (:width state))
        next-y (normalize (inc y) (:height state))
        prev-y (normalize (dec y) (:height state))]
    (list
      (list next-x next-y)
      (list next-x prev-y)
      (list prev-x next-y)
      (list prev-x prev-y)
      (list x next-y)
      (list x prev-y)
      (list next-x y)
      (list prev-x y))))

(defn should-live? [cell population state]
  (let [live-neighbors (filter
                         #(contains? population %)
                         (neighbors cell state))]
    (= 3 (count live-neighbors))))

(defn step [state]
  (let [population (:cells state)]
    (persistent!
      (reduce
        (fn [generation spawned]
          (conj! generation (first spawned)))
        (reduce
          (fn [survivors cell]
            (if (should-live? cell population state)
              survivors
              (disj! survivors cell)))
          (transient population)
          population)
        (filter
          #(= (second %) 3)
          (frequencies
            (remove #(contains? population %)
              (mapcat #(neighbors % state) population))))))))

(def life-game
  {:boot
     (fn [screen]
       {:cells (set `((16 13) (16 11) (14 12) (12 15) (16 12) (14 13) (10 16) (14 14) (12 16) (17 12)))
        :status :paused
        :width (first (:resolution screen))
        :height (last (:resolution screen))})

   :get-frame
     (fn [state screen]
       (let [cells (:cells state)
             color (if (= :paused (:status state)) :red :green)]
         (reduce #(graphix/draw %1 :pixel %2 color) screen cells)))

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
