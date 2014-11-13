(ns learnable.mouse
  (:require
    [om.core :as om :include-macros true]
    [cljs.core.async :as async :refer [put!]]))

(defn node-offset [node]
  [(.-offsetLeft node) (.-offsetTop node)])

(defn mouse-offset [e]
  [(.-pageX e) (.-pageY e)])

(defn controller [px screen-offset bus]
  (fn [e]
    (let [[sx sy] screen-offset
          [mx my] (mouse-offset e)]
      (put! bus
            [:mouse
             [(Math/floor (/ (- mx sx) px))
              (Math/floor (/ (- my sy) px))]]))))
