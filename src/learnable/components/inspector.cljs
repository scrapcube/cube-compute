(ns learnable.components.inspector
  (:require [learnable.cube.process :as ps]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn restore-to! [process this-time]
  (om/transact! process #(ps/restore % this-time)))

(defn entry-class [process at entry]
  (let [classes "timeline-entry "]
    (str classes
         (if (= at (get-in process [:log :now]))
           "current"
           ""))))

(defn timeline-entry [process at entry]
  (dom/li nil
    (dom/div #js {:className (entry-class process at entry)}
      (dom/a #js {:className (name (first entry))
                  :onClick (fn [_] (restore-to! process at))}
        ""))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "inspector"}
        (apply dom/ul #js {:className "timeline"}
          (cons
            (timeline-entry process 0 ["start" ""])
            (map-indexed
              #(timeline-entry process (inc %1) %2)
              (get-in process [:log :entries]))))

        (apply
          dom/div
          #js {:className "state"}
          (map
            (fn [[identifier value]]
              (dom/div
                #js {:className "attribute"}
                (dom/div {:className "name"} (name identifier))
                (dom/div {:className "value"} (str value))))
            (:state process)))))))
