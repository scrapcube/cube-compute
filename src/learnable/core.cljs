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

(defn learnable-computer [the-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)})

    om/IDidMount
    (did-mount [_]
      (om/transact! the-state (fn [_] ["foobar"])))

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (dom/div
        #js {:id "learnable-computer"}
        (om/build cube-manifestation/ui
                  (:the-cube the-state)
                  {:init-state {:bus bus}})
        (dom/div #js {:className "editor"})))))

(om/root
  learnable-computer
  app-state
  {:target (. js/document (getElementById "app"))})


