(ns learnable.core
  (:require [learnable.computer :as computer]
            [learnable.snake :as snake]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def dim [512 512])
(def res [16 16])

(def supervisor (computer/assemble-grid-computer "px" dim res 2))

(def app-state (atom (computer/run-program supervisor snake/snake-program)))

(om/root
  computer/ui
  app-state
  {:target (. js/document (getElementById "app"))})
