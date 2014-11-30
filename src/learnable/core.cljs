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
    :program (cube/grid-game default-game)}))

(defn reboot! [app-state]
  (om/transact! app-state
    (fn [state]
      (let [{:keys [the-cube program]} state]
        (assoc state :the-cube (cube/run-logged the-cube program))))))

(defn learnable-computer [app-state owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)})

    om/IDidMount
    (did-mount [_]
      (reboot! app-state))

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (dom/div
        #js {:id "learnable-computer"}
        (om/build cube-manifestation/ui
                  (:the-cube app-state)
                  {:init-state {:bus bus}})
        (dom/div #js {:className "editor"})))))

(om/root
  learnable-computer
  app-state
  {:target (. js/document (getElementById "app"))})


