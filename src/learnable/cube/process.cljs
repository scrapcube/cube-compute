(ns learnable.cube.process
  (:require [learnable.statelog :as statelog]))

(enable-console-print!)

(defn launch [program screen]
  (let [start-state ((:boot program) screen)]
    {:status :halted
     :get-frame (:get-frame program)
     :state start-state
     :transitions (:transitions program)
     :log (statelog/create start-state)}))

(defn transition [process]
  (fn [state [type input]]
    ((get-in process [:transitions type]) state input)))

(defn logtime [process]
  (get-in process [:log :now]))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (assoc process
    :status :running
    :log (statelog/trim (:log process))))

(defn restore [process at]
  (let [{:keys [log transitions]} process]
    (assoc process
      :state (statelog/replay log at (proc/transition process))
      :log (statelog/settime log at))))

(defn commit [process entry]
  (let [{:keys [state log]} process]
    (assoc process
      :state ((proc/transition process) state entry)
      :log (statelog/commit log entry))))

(defn output [process screen]
  ((:get-frame process) (:state process) screen))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [process]
  (= :running (:status process)))
