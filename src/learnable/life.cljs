(ns learnable.life
  (:require (learnable.display :as display)))

(defn neighbors [square population]
  (let [[x y] square]
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

(defn should-spawn? [square population]
  (let [ncount (count (neighbors cell population))]
    (= ncount 3)))

(defn surrounding [square]
  (let [[x y] square]
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
       {:cells (list)})
   :draw
     (fn [state screen]
       (reduce #(display/draw-pixel % % :green)
               screen
               (:cells state)))
   :clock
     (fn [state _]
       (let [population (:cells state)
             nextgen (set)]
         (into (list)
          (reduce (fn [nextgen cell]
                    (let [live
                            (reduce
                               (fn [nextgen dead-cell]
                                 (if (should-spawn? dead-cell population)
                                   (conj dead-cell nextgen)
                                   nextgen))
                               nextgen
                               (surrounding cell))]
                     (if (should-live? cell)
                       (conj cell live)
                       live)))
                  nextgen
                  population))))
   :mouse
     (fn [state point]
       (update-in state :cells #(conj % point)))
   :keyboard (fn [s _] s)})
