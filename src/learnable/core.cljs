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

(defn launch [cube program]
  (cube/run-logged the-cube (cube/grid-game program)))

(def app-state
  (atom {
    :the-cube (launch
                {:screen (graphix/surface :canvas :main `(0 0) `(512 512))
                 :hz 5
                 :process nil
                 :status :halted}
                default-game)
    :program  default-game}))

(defn reboot! [the-state]
  (om/transact! the-state :the-cube
    (fn [the-cube]
      (launch the-cube (:program @the-state)))))

(defn learnable-computer [the-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)})

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (dom/div
        #js {:id "learnable-computer"}
        (dom/a #js {:className "reboot"
                    :onClick   (fn [_] (reboot! the-state))})
        (om/build cube-manifestation/ui
                  (:the-cube the-state)
                  {:init-state {:bus bus}})
        (dom/div #js {:className "editor"})))))

(om/root
  learnable-computer
  app-state
  {:target (. js/document (getElementById "app"))})


