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

(defn calculate-pixel-ratio [unit-measure min-separation screen-width log]
  (let [differentials  (statelog/time-differentials log)]
    (max
      (/ (+ (* 2.0 unit-measure) min-separation) (average differentials))
      (/ (- screen-width (* 2.0 unit-measure)) (:log-time log)))))

(defn calculate-time-offset [total-time scrub-ratio screen-width pixel-ratio unit-measure]
  (let [time-in-screen (/ screen-width pixel-ratio)
        time-in-unit   (/ (* 2.0 unit-measure) pixel-ratio)]
    (* -1.0
       scrub-ratio
       pixel-ratio
       (- total-time
          time-in-screen
          (- time-in-unit)))))

;; each timeline-entry has a 2.0 unit-measure sized width and height.
(defn timeline-entry [entry owner]
  (let [{:keys [idx
                entry-time
                entry-type
                pixel-ratio
                current-idx]} entry
        css-class (str "timeline-entry "
                       (name entry-type)
                       (when (= idx current-idx)
                         " current"))]
    (reify
      om/IInitState
      (init-state [_] {:active false})

      om/IRenderState
      (render-state [_ {:keys [restore-chan active]}]
        (dom/li #js {:key              idx
                     :className        css-class
                     :onClick          (fn [_] (put! restore-chan idx))
                     :onMouseEnter     (fn [_] (om/set-state! owner :active true))
                     :onMouseLeave     (fn [_] (om/set-state! owner :active false))
                     :style #js {:left (* pixel-ratio entry-time)}}
          "")))))

(defn build-entries-list [log px-ratio]
  (cons
    {:idx          0
     :current-idx  (:now log)
     :entry-time   0
     :entry-type   :start
     :pixel-ratio  px-ratio}
    (map-indexed
      (fn [idx [etype _ etime]]
        {:idx          (inc idx)
         :current-idx  (:now log)
         :entry-time   etime
         :entry-type   etype
         :pixel-ratio  px-ratio})
      (:entries log))))

(defn timeline [process owner]
  (reify
    om/IInitState
    (init-state [_]
      {:time-offset           0
       :unit-measure          8
       :min-circle-separation 10
       :px-ratio              0
       :restore-chan          (chan)
       :scrub-chan            (chan)})

    om/IRenderState
    (render-state
      [_ {:keys [time-offset
                 restore-chan
                 scrub-chan
                 px-ratio
                 unit-measure]}]

      (dom/div #js {:className "timeline-material"}
        (dom/hr #js {:className "teal-blue-seam"} nil)
        (om/build scrubber/ui [] {:init-state {:scrub-chan scrub-chan}})

        (dom/div #js {:className "timeline"}
          (apply dom/ul #js {:className  "timeline-track"
                             :style #js  {:left time-offset}}
            (om/build-all
              timeline-entry
              (build-entries-list (:log process) px-ratio)
              {:init-state {:restore-chan restore-chan}}))

          (dom/div #js {:className "timeline-ruler"}
            (om/build ruler/ui
              {:total-time   (ps/time-of process)
               :time-offset  time-offset
               :pixel-ratio  px-ratio})))))

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
                (let [{:keys [screen-width unit-measure px-ratio]} (om/get-state owner)]
                  (calculate-time-offset
                    (ps/time-of @process)
                    scrub-ratio
                    screen-width
                    px-ratio
                    unit-measure))))
            (recur))))))

    om/IDidMount
    (did-mount [_]
      (let [{:keys [unit-measure
                    px-ratio
                    min-circle-separation]} (om/get-state owner)
            log           (:log @process)
            screen-width  (.-offsetWidth (om/get-node owner))
            pixel-ratio   (calculate-pixel-ratio
                            unit-measure
                            min-circle-separation
                            screen-width
                            log)]
        (when (not= pixel-ratio px-ratio)
          (om/update-state! owner
                  (fn [state]
                    (assoc state
                           :screen-width
                             screen-width
                           :px-ratio
                             pixel-ratio))))))))

(defn ui [process owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "interface"}
        (om/build timeline process)))))
