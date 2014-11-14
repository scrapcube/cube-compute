(ns learnable.life
  (:require [learnable.display :as display]))

(defn normalize [n m]
  (if (>= n 0)
    (mod n m)
    (+ (mod n m) m)))

(defn neighbors [cell population state]
  (let [[x y] cell
        {:keys [width height]} state]
    (filter
      (fn [[cx cy]]
        (let [dx (Math/abs (- cx x))
              dy (Math/abs (- cy y))]
          (and
            (or
              (<= dx 1)
              (= dx (dec width)))
            (or
              (<= dy 1)
              (= dy (dec height))))))
      (disj population cell))))

(defn should-live? [cell population state]
  (let [n (count (neighbors cell population state))]
    (or (= n 2) (= n 3))))

(defn surrounding [cell state]
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
              (mapcat #(surrounding % state) population))))))))

(def life-game
  {:boot
     (fn [screen]
       {:cells (set `((16 13) (16 11) (14 12) (12 15) (16 12) (14 13) (10 16) (14 14) (12 16) (17 12)))
        :status :paused
        :width (first (:resolution screen))
        :height (last (:resolution screen))})

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
