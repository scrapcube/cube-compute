(ns learnable.components.inspector
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (apply
        dom/div
        #js {:id "process-inspector"}
        (map
          (fn [[identifier value]]
            (dom/div
              #js {:className "attribute"}
              (dom/div {:className "name"} (name identifier))
              (dom/div {:className "value"} (str value))))
          (:state process))))))
