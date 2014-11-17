(ns learnable.components.controls
  (:require [learnable.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn ui [process owner]
  (reify
    IRender
    (render [_]
      (dom/div
        #js {:id "process-controls"}
        (dom/a
          #js {:id "play"
               :href "#"
               :onClick (fn [_]
                          (om/transact! process proc/resume))}
          "Play")
        (dom/a
          #js {:id "pause"
               :href "#"
               :onClick (fn [_]
                          (om/transact! process proc/halt))}
          "Pause")))))
