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

(def life-game
  {:boot
     (fn [screen]
       {:cells `((10 10) (11 11) (10 11))})

   :draw
     (fn [state screen]
       (reduce #(display/draw-pixel % % :green)
               screen
               (:cells state)))
   :transitions
      {:clock
        (fn [state _]
          (assoc state :cells
            (let [population (:cells state)]
              (into ()
                (reduce
                  (fn [nextgen cell]
                    (let [live (reduce
                                 (fn [nextgen dead-cell]
                                   (if (should-spawn? dead-cell population)
                                     (conj nextgen dead-cell)
                                     nextgen))
                                 nextgen
                                 (surrounding cell))]
                      (if (should-live? cell population)
                        (conj live cell)
                        live)))
                  (set `())
                  population)))))
      :mouse
        (fn [state point]
          (update-in state [:cells] #(conj % point)))
      :keyboard (fn [s _] s)}})
