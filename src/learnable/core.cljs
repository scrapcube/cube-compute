(ns learnable.core
  (:require [learnable.vcomputer :as vcomputer]
            [learnable.snake :as snake]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(enable-console-print!)

(def dim [512 512])
(def res [16 16])

(def supervisor (vcomputer/assemble-grid-computer "px" dim res 2))

(def app-state (atom (vcomputer/run-program supervisor snake/snake-program)))

(om/root
  vcomputer/vcomponent
  app-state
  {:target (. js/document (getElementById "app"))})
