(ns learnable.components.timeline
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn display-entry [at entry]
  (let [[type input] entry]
    (str at "-" entry)))

(defn timeline-entry [process at entry]
  (dom/li
    #js {:className (when (= at (proc/logtime process)))}
    (dom/a
      #js {:onClick (fn [_] (om/transact! process #(proc/restore % at)))}
      (display-entry at entry))))

(defn ui [process owner]
  (reify
    IRender
    (render [_]
      (let [entry (partial timeline-entry (proc/logtime process))]
        (apply
          dom/ul
          #js {:id "timeline"}
          (reverse
            (cons
              (entry 0 ["system" "start"])
              (map-indexed
                entry
                (:log process)))))))))
