(ns learnable.components.controls
  (:require [learnable.cube.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div
        #js {:id "process-controls"}
        (dom/a
          #js {:id "play"
               :href "#"
               :onClick
                 (fn [_]
                   (om/transact! process :status (constantly :running)))}
          "Play")
        (dom/a
          #js {:id "pause"
               :href "#"
               :onClick
                 (fn [_]
                   (om/transact! process :status (constantly :halted)))}
          "Pause")))))
