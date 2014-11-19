(ns learnable.components.info
  (:require [learnable.cube.core :as cube :refer [overclock throttle]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn ui [a-cube owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [hz process]} a-cube]
        (dom/div
          #js {:id "process-info"}
          (dom/h1 nil "Clock speed")

          (dom/div
            #js {:id "clock-speed"}
            (str hz "Hz"))

          (dom/a
            #js {:className "button decrease"
                 :onClick
                   (fn [_]
                     (om/transact! a-cube :hz throttle))}
                "Throttle clock speed.")
          (dom/a
            #js {:className "button increase"
                 :onClick (fn [_] (om/transact!
                                    a-cube
                                    :hz
                                    overclock))}
                "Increase clock speed."))))))
