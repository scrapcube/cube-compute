(ns learnable.core
  (:require [learnable.cube.core :as cube]
            [learnable.cube.graphix :as graphix]
            [learnable.components.cube :as cube-manifestation]
            [learnable.games.life :as life]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [chan]]))

(enable-console-print!)

(def box
  {:screen (graphix/surface :canvas :main `(0 0) `(512 512))
   :hz 5})

(defn boot [program]
  (cube/run-logged box program))

(def game life/life-game)

(def cube-state
  (atom (boot (cube/grid-game game))))

(defn reboot [js-code]
  (js/eval js-code)
  (swap! cube-state (boot (cube/grid-game game))))

(defn learnable-computer [cube-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)})

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (dom/div
        #js {:id "learnable-computer"}
        (om/build cube-manifestation/ui
                  cube-state
                  {:init-state {:bus bus}})
        (dom/div #js {:className "editor"})))))

(om/root
  learnable-computer
  cube-state
  {:target (. js/document (getElementById "app"))})


