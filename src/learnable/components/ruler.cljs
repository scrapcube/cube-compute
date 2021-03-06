(ns learnable.components.ruler
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

; the owner has a ref'ed child "ruler-canvas"
(defn draw! [canvas total-time pixel-ratio]
  (let [ctx            (.getContext canvas "2d")
        canvas-width   (.-width canvas)
        canvas-height  (.-height canvas)
        maximum-time   (Math/ceil (/ total-time 1000))]
    (set! (.-strokeStyle ctx) "rgb(10, 29, 71)")
    (set! (.-lineWidth ctx) 1)
    (set! (.-font ctx) "14px Roboto")
    (set! (.-textBaseline ctx) "bottom")
    (set! (.-globalAlpha ctx) 1.0)
    (.clearRect ctx 0 0 canvas-width canvas-height)
    (doseq [interval (map #(* % 1000) (range 0 maximum-time))]
      (let [second-mark       (* interval pixel-ratio)
            half-mark         (* (+ interval 500) pixel-ratio)
            millisecond-marks (map #(* (+ % interval) pixel-ratio)
                                   [100 200 300 400 600 700 800 900])]
        (.beginPath ctx)
        (.moveTo ctx second-mark 0)
        (.lineTo ctx second-mark canvas-height)
        (.moveTo ctx half-mark 0)
        (.lineTo ctx half-mark (* canvas-height 0.5))
        (doseq [millisecond-mark millisecond-marks]
          (.moveTo ctx millisecond-mark 0)
          (.lineTo ctx millisecond-mark (* canvas-height 0.25)))
        (.stroke ctx)
        (.strokeText ctx
                     (str (/ interval 1000) "s")
                     (+ second-mark 5)
                     (- canvas-height 1))))))

(defn ui [ruler-options owner]
  (let [{:keys [total-time time-offset pixel-ratio]} ruler-options]
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
               :height "32px"
               :style #js {:left time-offset}})))))
