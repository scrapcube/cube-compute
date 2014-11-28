(ns learnable.components.inspector
  (:require [learnable.cube.process :as ps]
            [learnable.cube.statelog :as statelog]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn average [coll]
  (/ (reduce + 0 coll) (count coll)))

(defn average-circle-spacing-ratio [radius min-separation screen-width log]
  (let [diameter (* 2.0 radius)
        differentials (statelog/time-differentials log)]
    (max
      (/ (+ diameter min-separation) (average differentials))
      (/ (- screen-width diameter) (:log-time log)))))

(defn scrubber [_ owner]
  (reify
    IRenderState
    (render-state [_ _]
      (dom/div #js {:className "scrubber shadow-2"}
        (dom/div #js {:className "scrubber-track"}
          (dom/div #js {:className "scrubber-knob shadow-2"
                        :style #js {:left 0}}
            (dom/i #js {:className "fa fa-clock-o"})))))))

(defn timeline-entry [entry owner]
  (let [{:keys [idx entry-time entry-type pixel-ratio]} entry]
    (reify
      om/IRenderState
      (render-state [_ {:keys [restore-chan]}]
        (dom/li #js {:className (str "timeline-entry " (name entry-type))
                     :onClick (fn [_] (put! restore-chan idx))
                     :style #js {:left (* pixel-ratio entry-time)}}
          "")))))

(defn build-entries-list [log pixel-conversion-ratio]
  (cons
    {:idx (inc idx)
     :entry-time 0
     :entry-type :start
     :pixel-ratio pixel-conversion-ratio}
    (map-indexed
      (fn [idx [etype _ etime]]
        {:idx (inc idx)
         :entry-time etime
         :entry-type etype
         :pixel-ratio pixel-conversion-ratio})
      (:entries log))))

(defn timeline [process owner]
  (reify
    om/IInitState
    (init-state [_]
      {:circle-radius 5
       :min-circle-separation 5
       :pixel-conversion-ratio 0.10
       :restore-chan (chan)})

    om/IRenderState
    (render-state
      [_ {:keys [time-offset scrub-chan pixel-conversion-ratio circle-radius]}]

      (div #js {:className "timeline-material"}
        (om/build scrubber [])

        (dom/hr #js {:className "teal-blue-seam"} nil)
        (apply dom/ul #js {:className "timeline-track"
                           :left time-offset}
          (om/build-all timeline-entry
            (build-entries-list (:log process) pixel-conversion-ratio)))
        (dom/div #js {:className "timeline-rules"}
          (dom/div #js {:className "timeline-ruler-marks"
                        :style #js {:left time-offset}}))))

    om/IWillMount
    (will-mount [_]
      (go (loop []
        (let [entry-idx (<! restore-chan)]
          (om/transact! process #(proc/restore % entry-idx))
          (recur)))))

    om/IDidMount
    (did-mount [_]
      (om/update-state! owner
        (fn [state]
          (let [log (:log @process)
                {:keys [circle-radius min-circle-separation]} state
                screen-width (.-offsetWidth (om/get-node owner))]
            (assoc state
              :pixel-conversion-ratio
              (average-circle-spacing-ratio
                circle-radius
                min-circle-separation
                screen-width
                log))))))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "interface"}
        (om/build timeline process)))))
