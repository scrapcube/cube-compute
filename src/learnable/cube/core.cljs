(ns learnable.cube.core
  (:require
    [learnable.cube.process :as ps]
    [learnable.cube.graphix :as graphix]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :as async :refer [put!]]))

(enable-console-print!)

(defn grid-game [game]
  (let [{:keys [boot transitions get-frame]} game
        {:keys [mouse]} transitions
        grid (graphix/surface :grid :game [0 0] [512 512] [32 32])]
    (assoc
      game
      :boot (fn [_] (boot grid))
      :get-frame
        (fn [state screen]
          (graphix/blit screen (get-frame state grid)))
      :transitions
        (assoc transitions
          :mouse
          (fn [state [graphic-id point]]
            (str "T: " graphic-id " - " (str point))
            (if (= :game graphic-id)
              (mouse state point)
              state))))))

(def max-hertz 20)

(defn admit [a-cube entry]
  (let [current-time (:current-time a-cube)
        time-offset (- (js/Date.now) current-time)]
    (if (= :running (:status a-cube))
      (update-in a-cube [:process] #(ps/commit % entry time-offset))
      a-cube)))

(defn halt [a-cube]
  (assoc a-cube
    :status :halted
    :current-time 0))

(defn resume [a-cube]
  (let [process (:process a-cube)
        previous-time (:current-time a-cube)
        current-time (js/Date.now)
        differential (- current-time
                        previous-time
                        (reduce #(+ %1 (last %2)) 0 (get-in process [:log :entries])))]
    (assoc a-cube
      :status :running
      :current-time (js/Date.now)
      :process
        (update-in process [:log :entries]
          (fn [entries]
            (vec
              (map
                (fn [entry]
                  (conj (subvec entry 0 3)
                        (+ differential (last entry))))
                entries)))))))



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
        (put! bus [:keyboard keystroke])))))

(defn mouse-controller [bus]
  (fn [transform]
    (fn [e]
      (let [mouse-point (list (.-pageX e) (.-pageY e))
            point (transform mouse-point)]
        (when (not= nil point)
          (put! bus [:mouse (vec point)]))))))

(defn run-logged [box program]
  (assoc box
         :process
         (ps/launch program (:screen box))))
