(ns learnable.components.inspector
  (:require [learnable.cube.process :as ps]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn track-position [entry-type circle-radius]
  (* 2 circle-radius (condp = entry-type
                       :clock 1
                       :mouse 3
                       :start 3
                       :keyboard 5)))

(defn timeline-entry [process at entry pixel-ratio circle-radius]
  (dom/li #js {:style #js {:left (* pixel-ratio (last entry))
                           :width (* 2 circle-radius)}}
    (dom/a #js {:className "timeline-entry"
                :key at
                :onClick
                  (fn [_]
                    (om/transact! process #(ps/restore % at)))
                :style
                  #js {:top (track-position (first entry) circle-radius)
                       :width (* 2 circle-radius)
                       :height (* 2 circle-radius)
                       :borderRadius circle-radius}}
      "")))

(defn timeline [process owner]
  (reify
    om/IInitState
    (init-state [_]
      {:circle-radius 5
       :min-circle-separation 0
       :pixel-conversion-ratio 0.10})

    om/IWillMount
    (will-mount [_]
      (om/update-state!
        owner
        (fn [state]
          (let [entries (get-in process [:log :entries])
                {:keys [circle-radius min-circle-separation]} state
                differentials
                  (map-indexed
                    #(- (last %2) (last (nth entries %1)))
                    entries)
                min-time-differential
                  (reduce min (first differentials) (rest differentials))]
            (assoc state
              :pixel-conversion-ratio
                (/ (+ (* 2 circle-radius) min-circle-separation)
                   min-time-differential))))))

    om/IRenderState
    (render-state [_ {:keys [pixel-conversion-ratio circle-radius]}]
      (apply dom/ul #js {:className "timeline"
                         :height (* 2 circle-radius 7)}
        (cons
            (timeline-entry process 0 ["start" "" 20] pixel-conversion-ratio circle-radius)
            (map-indexed
              #(timeline-entry process (inc %1) %2 pixel-conversion-ratio circle-radius)
              (get-in process [:log :entries])))))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "inspector"}
        (om/build timeline process)

        (apply
          dom/div
          #js {:className "state"}
          (map
            (fn [[identifier value]]
              (dom/div
                #js {:className "attribute"}
                (dom/div {:className "name"} (name identifier))
                (dom/div {:className "value"} (str value))))
            (:state process)))))))
