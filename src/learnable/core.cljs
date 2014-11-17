(ns learnable.core
  (:require [learnable.cube.core :as cube]
            [learnable.components.cube :as cube-manifestation]
            [learnable.games.life :as life]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def box
  {:screen (graphix/surface :canvas :main `(0 0) `(512 512))
   :hz 5})

(defn boot [program]
  (cube/run-logged box program))

(def game (cube/grid-game life/life-game))

(def cube-state
  (atom (boot (cube/grid-game game))))

(defn reboot [js-code]
  (js/eval js-code)
  (swap! cube-state (boot (cube/grid-game game))))

(om/root
  cube-manifestation/ui
  cube-state
  {:target (. js/document (getElementById "app"))})
