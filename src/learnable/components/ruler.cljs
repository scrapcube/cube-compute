(ns learnable.components.ruler
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn draw-interval! [ctx i pixel-ratio canvas-height]
  (println (str "drawing interval: " i))
  (println (str "with pixel-ratio " pixel-ratio))
  (println (str "with canvas-height " canvas-height))
  (.moveTo ctx (* i pixel-ratio) 0)
  (.lineTo ctx (* i pixel-ratio) canvas-height)
  (.moveTo ctx (* (+ i 500) pixel-ratio) 0)
  (.lineTo ctx (* (+ i 500) pixel-ratio) (* 0.75 canvas-height)))

(defn ui [ruler-info owner]
  (reify

  om/IDidMount
  (did-mount [_]
    (when (not= (:total-time ruler-info) 0)
        (let [canvas (.getElementById js/document "rulercanvas")
                  ctx (.getContext canvas "2d")]
              (aset ctx "strokeStyle" "rgb(10, 29, 71)")
              (aset ctx "lineWidth" 2)
              (.beginPath ctx)
              (doseq [i (range 0 (Math/ceil (/ (:total-time ruler-info) 1000.0)))]
                (draw-interval! ctx (* 1.0 i) (* 1.0 (:pixel-ratio ruler-info)) (aget canvas "height")))
              (.stroke ctx))))

  om/IRender
  (render [_]
    (dom/div #js {:className "timeline-ruler"}
      (dom/canvas
        #js {:id "rulercanvas"
             :width (str (/ (:total-time ruler-info) (:pixel-ratio ruler-info)) "px")
             :height "32px"})))))
