(ns learnable.cube.components.ruler
  (:require [om.core :as om :include-macros true]))

; the owner has a ref'ed child "ruler-canvas"
(defn draw! [owner total-time pixel-ratio]
  (let [canvas         (om/get-node owner "ruler-canvas")
        ctx            (.getCanvas canvas "2d")
        canvas-height  (.-height canvas)
        maximum-time   (Math/ceil (/ total-time))]
    (set! (.-strokeStyle ctx) "rbg(10, 29, 71)")
    (set! (.-lineWidth ctx) 2)
    (.beginPath ctx)
    (doseq [interval (range 0 maximum-time)]
      (let [second-mark       (* interval pixel-ratio)
            half-mark         (+ 500 second-mark)
            millisecond-marks (map #(+ % second-mark) [100 200 300 400 600 700 800 900])]
        (.moveTo ctx second-mark 0)
        (.lineTo ctx second-mark canvas-height)
        (.moveTo ctx half-mark 0)
        (.lineTo ctx half-mark (* canvas-height 0.75))
        (doseq [millisecond-mark millisecond-marks]
          (.moveTo ctx millisecond-mark 0)
          (.lineTo ctx millisecond-mark (* canvas-height 0.5)))))
    (.stroke ctx)))
