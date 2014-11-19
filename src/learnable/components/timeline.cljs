(ns learnable.components.timeline
  (:require [learnable.cube.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn display-entry [at entry]
  (let [[type input] entry]
    (str at "-" (str (name type) " : " (name input)))))

(defn timeline-entry [process at entry]
  (dom/li
    #js {:className (when (= at (get-in process [:log :now])))}
    (dom/a
      #js {:onClick (fn [_] (om/transact! process #(proc/restore % at)))}
      (display-entry at entry))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/ul #js {:id "timeline"}
        (reverse
          (cons
            (timeline-entry process 0 ["system" "start"])
            (map-indexed
              #(timeline-entry process %1 %2)
              (:log process))))))))
