(ns learnable.components.info
  (:require [learnable.cube :as cube :refer [overclock throttle]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn ui [computer owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [hz process]}]
        (dom/div
          #js {:id "process-info"}
          (dom/h1 nil "Hz")

          (dom/div
            #js {:id "clock-speed"}
            hz))

          (dom/div
            #js {:className "button decrease"
                 :onClick
                   (fn [_]
                     (om/transact! computer :hz throttle))})
          (dom/div
            #js {:className "button increase"
                 :onClick (fn [_] (om/transact!
                                    computer
                                    :hz
                                    overclock))})))))
