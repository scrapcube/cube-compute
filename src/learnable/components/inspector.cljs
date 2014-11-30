(ns learnable.components.inspector
  (:require [learnable.cube.process :as ps]
            [learnable.cube.statelog :as statelog]
            [learnable.components.scrubber :as scrubber]
            [learnable.components.ruler :as ruler]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put! <! chan]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn average [coll]
  (/ (reduce + 0 coll) (count coll)))

(defn average-circle-spacing-ratio [radius min-separation screen-width log]
  (let [diameter (* 2.0 radius)
        differentials (statelog/time-differentials log)]
    (max
      (/ (+ diameter min-separation) (average differentials))
      (/ (- screen-width diameter) (:log-time log)))))

(defn timeline-entry [entry owner]
  (let [{:keys [idx entry-time entry-type pixel-ratio current-idx]} entry]
    (reify
      om/IInitState
      (init-state [_]
        {:active false})

      om/IRenderState
      (render-state [_ {:keys [restore-chan active]}]
        (dom/li #js {:key idx
                     :className
                      (str "timeline-entry "
                        (name entry-type)
                        (if (= idx current-idx) " current shadow-3" (if active " shadow-2 " "")))
                     :onClick (fn [_] (put! restore-chan idx))
                     :onMouseEnter
                       (fn [_]
                        (om/set-state! owner :active true))
                     :onMouseLeave
                       (fn [_]
                        (om/set-state! owner :active false))
                     :style #js {:left (* pixel-ratio entry-time)}}
          "")))))

(defn build-entries-list [log pixel-conversion-ratio]
  (cons
    {:idx 0
     :current-idx (:now log)
     :entry-time 0
     :entry-type :start
     :pixel-ratio pixel-conversion-ratio}
    (map-indexed
      (fn [idx [etype _ etime]]
        {:idx (inc idx)
         :current-idx (:now log)
         :entry-time etime
         :entry-type etype
         :pixel-ratio pixel-conversion-ratio})
      (:entries log))))

(defn timeline [process owner]
  (reify
    om/IInitState
    (init-state [_]
      {:time-offset 0
       :circle-radius 8
       :min-circle-separation 10
       :pixel-conversion-ratio 0
       :restore-chan (chan)
       :scrub-chan (chan)})

    om/IRenderState
    (render-state
      [_ {:keys [time-offset
                 restore-chan
                 scrub-chan
                 pixel-conversion-ratio
                 circle-radius]}]
      (dom/div #js {:className "timeline-material"}
        (dom/hr #js {:className "teal-blue-seam"} nil)
        (om/build scrubber/ui [] {:init-state {:scrub-chan scrub-chan}})
        (dom/div #js {:className "timeline"}
          (apply dom/ul #js {:className "timeline-track"
                             :style #js {:left time-offset}}
            (om/build-all timeline-entry
              (build-entries-list (:log process) pixel-conversion-ratio)
              {:init-state {:restore-chan restore-chan}}))
          (dom/div #js {:className "timeline-ruler"}
            (let [total-time (get-in process [:log :log-time])]
              (om/build ruler/ui
                {:total-time total-time
                 :time-offset time-offset
                 :pixel-ratio pixel-conversion-ratio}))))))

    om/IWillMount
    (will-mount [_]
      (let [{:keys [restore-chan scrub-chan]} (om/get-state owner)]
        (go (loop []
          (let [entry-idx (<! restore-chan)]
            (om/transact! process #(ps/restore % entry-idx))
            (recur))))
        (go (loop []
          (let [scrub-ratio (<! scrub-chan)]
            (om/update-state! owner :time-offset
              (fn [state]
                (let [log-time (get-in @process [:log :log-time])
                      {:keys [screen-width circle-radius pixel-conversion-ratio]} (om/get-state owner)]
                  (* -1.0
                     scrub-ratio
                     pixel-conversion-ratio
                     (- log-time
                        (/ screen-width pixel-conversion-ratio)
                        (* -1.0 (/ (* 2 circle-radius) pixel-conversion-ratio)))))))
            (recur))))))

    om/IDidMount
    (did-mount [_]
      (let [log (:log @process)
            {:keys [circle-radius min-circle-separation pixel-conversion-ratio]} (om/get-state owner)
            screen-width (.-offsetWidth (om/get-node owner))
            pixel-ratio
              (average-circle-spacing-ratio
                circle-radius
                min-circle-separation
                screen-width
                log)]
        (when (not= pixel-ratio pixel-conversion-ratio)
          (om/update-state! owner
                  (fn [state]
                    (assoc state
                      :screen-width screen-width
                      :pixel-conversion-ratio pixel-ratio))))))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "interface"}
        (om/build timeline process)))))
