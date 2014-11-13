(ns learnable.life
  (:require (learnable.display :as display)))

(defn neighbors [tcell population]
  (let [[x y] tcell]
    (filter
      (fn [cell]
        (let [[cx cy] cell]
          (and
            (< (Math/abs (- cx x)) 2)
            (< (Math/abs (- cy y)) 2))))
      population)))

(defn should-live? [cell population]
  (let [ncount (count (neighbors cell population))]
    (or (= ncount 2) (= ncount 3))))

(defn should-spawn? [cell population]
  (let [ncount (count (neighbors cell population))]
    (= ncount 3)))

(defn surrounding [cell]
  (let [[x y] cell]
    (list (list (inc x) (inc y))
          (list (inc x) (dec y))
          (list (dec x) (inc y))
          (list (dec x) (dec y))
          (list x (inc y))
          (list x (dec y))
          (list (inc x) y)
          (list (dec x) y))))


(defn step [world]
  (let [population (set (:cells world))
        potentials (difference
                      (set (mapcat surrounding (:cells world)))
                      population)]
    (reduce (fn [generation cell]
              (if (should-live? cell population)
                (cons cell generation)
                generation))
            (reduce (fn [generation potential]
                      (if (should-spawn? potential population)
                        (cons potential generation)
                        generation))
                    (list)
                    potentials)
            population)))

(def life-game
  {:boot
     (fn [screen]
       {:cells `((10 10) (11 11) (10 11))})

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
          (update-in state [:cells] #(conj % point)))
      :keyboard (fn [state ks]
                  (when (= ks :key-up)
                    (assoc state
                      :status (if (= :paused (:status state))
                                :running
                                :paused))))}})
