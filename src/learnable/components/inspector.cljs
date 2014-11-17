(ns learnable.components.inspector)

(defn ui [process owner]
  (reify
    IRender
    (render [_]
      (apply
        dom/div
        #js {:id "process-inspector"}
        (map
          (fn [[identifier value]]
            (dom/div
              #js {:className "attribute"}
              (dom/div {:className "name"} (name identifier))
              (dom/div {:className "value"} (value))))
          (:state process))))))
