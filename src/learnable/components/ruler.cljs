(ns learnable.components.ruler
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

; the owner has a ref'ed child "ruler-canvas"
(defn draw! [canvas total-time pixel-ratio]
  (let [ctx            (.getContext canvas "2d")
        canvas-height  (.-height canvas)
        maximum-time   (Math/ceil (/ total-time 1000))]
    (set! (.-strokeStyle ctx) "rgb(10, 29, 71)")
    (set! (.-lineWidth ctx) 2)
    (set! (.-globalAlpha ctx) 1.0)
    (.beginPath ctx)
    (doseq [interval (map #(* % 1000) (range 0 maximum-time))]
      (let [second-mark       (* interval pixel-ratio)
            half-mark         (* (+ interval 500) pixel-ratio)
            millisecond-marks (map #(* (+ % interval) pixel-ratio)
                                   [100 200 300 400 600 700 800 900])]
        (.moveTo ctx second-mark 0)
        (.lineTo ctx second-mark canvas-height)
        (.moveTo ctx half-mark 0)
        (.lineTo ctx half-mark (* canvas-height 0.75))
        (doseq [millisecond-mark millisecond-marks]
          (.moveTo ctx millisecond-mark 0)
          (.lineTo ctx millisecond-mark (* canvas-height 0.5)))))
    (.stroke ctx)))

(defn ui [ruler-options owner]
  (let [{:keys [total-time pixel-ratio]} ruler-options]
    (reify
      om/IDidUpdate
      (did-update [_ _ _]
        (draw! (om/get-node owner "rulercanvas") total-time pixel-ratio))

      om/IRender
      (render [_]
        (dom/canvas
          #js {:id "ruler-canvas"
               :ref "rulercanvas"
               :width
                 (str (* total-time pixel-ratio) "px")
               :height "32px"})))))
