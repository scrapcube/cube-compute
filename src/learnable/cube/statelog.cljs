(ns learnable.cube.statelog)

(defn create [start-state]
  {:entries []
   :start-state start-state
   :now 0})

(defn commit [log entry]
  (let [{:keys [entries now]} log]
    (assoc log
           :entries (conj entries entry)
           :now (inc now))))

(defn trim [log]
  (let [{:keys [entries now]} log]
    (assoc log :entries (subvec entries 0 now))))

(defn replay [log atime f]
  (reduce f (:start-state log) (subvec (:entries log) 0 atime)))

(defn synced? [log]
  (or (= 0 now) (= now (count (:entries log)))))

(defn settime [log atime]
  (assoc log :now atime))
