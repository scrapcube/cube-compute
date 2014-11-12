(ns learnable.clock
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(enable-console-print!)

(def max-hertz 20)

(defn overclock [hz]
  (if (<= hz max-hertz)
    (if (< hz 1)
      (* 2 hz)
      (inc hz))
    hz))

(defn throttle [hz]
  (if (> hz 1)
    (dec hz)
    (/ hz 2)))

(defn ui [hz owner]
  (reify
    om/IInitState
    (init-state [_]
      {:timer 0})

    om/IWillMount
    (will-mount [_]
      (om/set-state!
        owner
        :timer
        (let [bus (om/get-state owner :bus)]
          (js/setInterval
            (fn [] (put! bus [:clock nil]))
            (* 1000 (/ 1.0 hz))))))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :timer)))

    om/IRenderState
    (render-state [_ state]
      (dom/div
        #js {:className "clock-speed screen"}
        (str hz "Hz")))))







