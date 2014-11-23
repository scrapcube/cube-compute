(ns learnable.cube.statelog)

(defn create [start-state]
  {:entries []
   :start-state start-state
   :now 0})

(defn commit [log entry]
  (let [{:keys [entries now last-time]} log
        current-time (js/Date.now)
        time-differential (- current-time last-time)]
    (assoc log
           :last-time current-time
           :entries (conj entries (conj entry time-differential))
           :now (inc now))))

(defn trim [log]
  (let [{:keys [entries now]} log]
    (assoc log :entries (subvec entries 0 now))))

(defn replay [log atime f]
  (reduce #(f %1 (butlast %2))
          (:start-state log)
          (subvec (:entries log) 0 atime)))

(defn synced? [log]
  (let [{:keys [now entries]} log]
    (or (and (= 0 now) (= 0 (count entries)))
        (= now (count entries)))))

(defn settime [log atime]
  (assoc log :now atime))

(defn total-time [log]
  (reduce #(+ % 1 (last %2)) 0 (:entries log)))
