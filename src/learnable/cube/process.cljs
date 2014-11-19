(ns learnable.cube.process
  (:require [learnable.cube.statelog :as statelog]))

(enable-console-print!)

(defn launch [program screen]
  (let [start-state ((:boot program) screen)]
    {:status :halted
     :get-frame (:get-frame program)
     :state start-state
     :transitions (:transitions program)
     :log (statelog/create start-state)}))

(defn transition [process]
  (fn [state [etype input]]
    (println (get-in process [:transitions etype]))
    ((get-in process [:transitions etype]) state input)))

(defn logtime [process]
  (get-in process [:log :now]))

(defn restore [process at]
  (let [{:keys [log transitions]} process]
    (assoc process
      :state (statelog/replay log at (transition process))
      :log (statelog/settime log at))))

(defn commit [process entry]
  (println "called commit")
  (let [{:keys [state log]} process
        tlog (if (statelog/synced? log)
              log
              (statelog/trim log))]
    (assoc process
      :state ((transition process) state entry)
      :log (statelog/commit tlog entry))))

(defn output [process screen]
  ((:get-frame process) (:state process) screen))
