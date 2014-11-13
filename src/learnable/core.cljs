(ns learnable.core
  (:require [learnable.computer :as computer]
            [learnable.life :as life]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def dim [512 512])
(def res [32 32])

(def supervisor (computer/assemble-grid-computer "px" dim res 10))

(def app-state (atom (computer/run-program supervisor life/life-game)))

(om/root
  computer/ui
  app-state
  {:target (. js/document (getElementById "app"))})
