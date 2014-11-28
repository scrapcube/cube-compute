(ns learnable.components.scrubber
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            (cljs.core.async :as async :refer [put!]))
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn move-scrubber [owner]
  (fn [e]
    (let [{:keys [held knob-width track-width scrub-chan]} (om/get-state owner)
          track-node (aget (.getElementsByClassName js/document "scrubber-track") 0)
          track-position (.-offsetLeft track-node)
          mouse-position (.-clientX e)
          knob-radius (/ knob-width 2.0)
          knob-offset (- mouse-position track-position)]
      (when (= true held)
        (put! scrub-chan (/ knob-offset (- track-width knob-width)))
        (om/update-state! owner
          (fn [state]
            (assoc state
              :knob-offset
                (cond (> knob-offset (- track-width knob-width))
                      (- track-width knob-width)
                      (< knob-offset 0)
                      0
                      :else (- knob-offset knob-radius)))))))))

(defn ui [_ owner]
  (reify
    om/IInitState
    (init-state [_]
      {:held false
       :knob-offset 0})

    om/IDidMount
    (did-mount [_]
      (let [knob-node (aget (.getElementsByClassName js/document "scrubber-knob") 0)
            track-node (aget (.getElementsByClassName js/document "scrubber-track") 0)
            knob-width (.-outerWidth knob-node)]
        (om/update-state!
          owner
          (fn [state]
            (assoc state
              :knob-width knob-width
              :track-width (.-outerWidth track-node))))))

    om/IRenderState
    (render-state [_ {:keys [knob-offset held scrub-chan]}]
      (dom/div #js {:className "scrubber shadow-2"}
        (dom/div #js {:className "scrubber-track"}
          (dom/div #js {:className "scrubber-knob shadow-2"
                        :onMouseDown
                          (fn [_]
                            (om/set-state! owner :held true))
                        :onMouseUp
                          (fn [_]
                            (om/set-state! owner :held false))
                        :onMouseMove (move-scrubber owner)
                        :style #js {:left knob-offset
                                    :background
                                      (if held
                                        "#666"
                                        "")}}
            (dom/i #js {:className "fa fa-clock-o"})))))))
