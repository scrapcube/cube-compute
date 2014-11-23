(ns learnable.cube.statelog)

(defn create [start-state]
  {:entries []
   :start-state start-state
   :now 0})

(defn commit [log entry time-offset]
  (let [{:keys [entries now]} log]
    (assoc log
           :entries (conj entries (conj entry time-offset))
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
