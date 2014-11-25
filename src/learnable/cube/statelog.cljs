(ns learnable.cube.statelog)

(defn create [start-state]
  {:entries []
   :start-state start-state
   :last-time (js/Date.now)
   :log-time 0
   :now 0})

(defn commit [log entry]
  (let [{:keys [entries now last-time log-time]} log
        current-time (js/Date.now)
        time-differential (- current-time last-time)
        new-log-time (+ log-time time-differential)]
    (assoc log
           :last-time current-time
           :log-time new-log-time
           :entries (conj entries (conj entry new-log-time))
           :now (inc now))))

(defn trim [log]
  (let [{:keys [entries now]} log
        trimmed-entries (vec (subvec entries 0 now))]
    (assoc log
      :entries trimmed-entries
      :log-time (last (last trimmed-entries)))))

(defn replay [log atime f]
  (reduce #(f %1 (butlast %2))
          (:start-state log)
          (subvec (:entries log) 0 atime)))

(defn synced? [log]
  (let [{:keys [now entries]} log]
    (or (and (= 0 now) (= 0 (count entries)))
        (= now (count entries)))))

(defn set-event-index [log atime]
  (assoc log :now atime))

(defn set-time [log]
  (assoc log :last-time (js/Date.now)))

(def time-differentials [log]
  (let [entries (:entries log)]
    (map-indexed
      (fn [idx entry]
        (- (last entry) (last (nth entries idx))))
      (rest entries))))
