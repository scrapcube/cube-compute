(ns learnable.components.controls
  (:require [learnable.cube.core :as cube]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn button [text action active?]
  (dom/a
    #js {:className
           (str "button" (if (active?) " active" ""))
         :href "#"
         :onClick (if (active?)
                    (constantly nil)
                    (fn [_] (action)))}
      text))

(defn ui [a-cube owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "cube-control toolbar"}
        (if (= (:status a-cube) :running)
          (dom/a #js {:className "action-button"
                      :onClick (fn [_] (om/transact! a-cube cube/resume))}
            (dom/i #js {:className "fa fa-pause"}))
          (dom/a #js {:className "action-button"
                      :onClick (fn [_] (om/transact! a-cube cube/halt))}
            (dom/i #js {:className "fa fa-play"})))))))
