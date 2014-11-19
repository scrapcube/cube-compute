(ns learnable.cube.core
  (:require
    [learnable.cube.process :as proc]
    [learnable.cube.graphix :as graphix]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :as async :refer [put!]])
  (:require-macros
    [cljs.core.async.macros :as async-mac :refer [go]]))

(enable-console-print!)

(defn grid-game [game]
  (let [{:keys [boot transitions get-frame]} game
        {:keys [mouse]} transitions
        grid (graphix/surface :grid :game `(0 0) `(512 512) `(32 32))]
    (assoc
      game
      :boot (fn [_] (boot grid))
      :get-frame #(graphix/blit %2 (get-frame %1 grid))
      :transitions
        (assoc transitions
          :mouse
          (fn [state [graphic-id point]]
            (if (= :game graphic-id)
              (mouse state point)
              state))))))

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

(defn keyboard-controller [bus]
  (fn [e]
    (let [keystroke
            (condp = (.-keyCode e)
              38 :up
              39 :right
              40 :down
              37 :left
              27 :esc
              32 :space
              nil)]
      (when (not= nil keystroke)
        (put! bus keystroke)))))

(defn mouse-controller [bus]
  (fn [transform]
    (fn [e]
      (let [mouse-point (list (.-pageX e) (.-pageY e))
            point (transform mouse-point)]
        (when point
          (put! bus point))))))

(defn run-logged [box program]
  (assoc box
         :process
         (proc/launch program (:screen box))))
