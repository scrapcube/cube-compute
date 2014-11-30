(ns learnable.core
  (:require [learnable.cube.core :as cube]
            [learnable.cube.graphix :as graphix]
            [learnable.components.cube :as cube-manifestation]
            [learnable.games.life :as life]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [chan]]))

(enable-console-print!)

(def default-game life/life-game)

(def app-state
  (atom {
    :the-cube {:screen (graphix/surface :canvas :main `(0 0) `(512 512))
               :hz 5
               :process nil
               :status :halted}
    :program default-game}))

(defn reboot! [the-state]
  (let [program (:program @the-state)]
    (om/transact! the-state :the-cube
      (fn [the-cube]
        (let [prime (cube/run-logged the-cube (cube/grid-game program))]
          (println "BOOTED_STATE: ")
          (println prime)
          prime)))))

(defn learnable-computer [the-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)})

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (println the-state)
      (dom/div
        #js {:id "learnable-computer"}
        (om/build cube-manifestation/ui
                  (:the-cube the-state)
                  {:init-state {:bus bus}
                   :fn (fn [cube-state]
                         (cube-run-logged cube-state (cube/grid-game (:program the-state))))})
        (dom/div #js {:className "editor"})))))

(om/root
  learnable-computer
  app-state
  {:target (. js/document (getElementById "app"))})


