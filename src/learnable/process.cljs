(ns learnable.process
  (:require [learnable.statelog :as statelog]))

(enable-console-print!)

(defn launch [program screen]
  (let [start-state ((:boot program) screen)]
    {:status :halted
     :draw (:draw program)
     :state start-state
     :transitions (:transitions program)
     :log (statelog/create start-state)}))

(defn transition [process]
  (fn [state [type input]]
    ((get-in process [:transitions type]) state input)))

(defn halt [process]
  (assoc process :status :halted))

(defn resume [process]
  (assoc process
    :status :running
    :log (statelog/trim (:log process))))

(defn restore [process at]
  (let [{:keys [log transitions]} process]
    (assoc process
      :state (statelog/replay log at (transition process))
      :log (statelog/settime log at))))

(defn commit [process entry]
  (let [{:keys [state log]} process]
    (assoc process
      :state ((transition process) state entry)
      :log (statelog/commit log entry))))

(defn output [process screen]
  ((:draw process) (:state process) screen))

(defn halted? [process]
  (= :halted (:status process)))

(defn running? [process]
  (= :running (:status process)))
