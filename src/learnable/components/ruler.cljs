(ns learnable.components.ruler
  (:require [om.core :as om :include-macros true]))

; the owner has a ref'ed child "ruler-canvas"
(defn draw! [canvas total-time pixel-ratio]
  (let [ctx            (.getContext canvas "2d")
        canvas-height  (aget canvas "height")
        maximum-time   (Math/ceil (/ total-time 1000))]
    (println "...drawing ruler...")
    (aset ctx "strokeStyle" "rgb(10, 29, 71)")
    (aset ctx "lineWidth" 2)
    (aset ctx "globalAlpha" 1.0)
    (.beginPath ctx)
    (doseq [interval (map #(* % 1000) (range 0 maximum-time))]
      (let [second-mark       (* interval pixel-ratio)
            half-mark         (* (+ interval 500) pixel-ratio)
            millisecond-marks (map #(* (+ % second-mark) pixel-ratio)
                                   [100 200 300 400 600 700 800 900])]
        (.moveTo ctx second-mark 0)
        (.lineTo ctx second-mark canvas-height)
        (.moveTo ctx half-mark 0)
        (.lineTo ctx half-mark (* canvas-height 0.75))
        (doseq [millisecond-mark millisecond-marks]
          (.moveTo ctx millisecond-mark 0)
          (.lineTo ctx millisecond-mark (* canvas-height 0.5)))))
    (.stroke ctx)))
