(ns learnable.components.cube
  (:require [learnable.cube.process :as ps]
            [learnable.cube.core :as cube]
            [learnable.components.display :as display]
            [learnable.components.inspector :as inspector]
            [learnable.components.controls :as controls]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put! <!]])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(enable-console-print!)

(defn clock [hz owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [bus (om/get-state owner :bus)]
        (om/set-state! owner :timer
          (js/setInterval
            (fn [] (put! bus [:clock nil]))
            (* 1000 (/ 1.0 hz))))))

    om/IWillUnmount
    (will-unmount [_]
      (js/clearInterval (om/get-state owner :timer)))

    om/IRenderState
    (render-state [_ _] (dom/span #js {:className "clock hidden"} ""))))

(defn ui [a-cube owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [bus (om/get-state owner :bus)]
        (go (loop []
          (let [entry (<! bus)]
            (om/transact! a-cube #(cube/admit % entry))
            (recur))))))

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (let [{:keys [process screen hz]} a-cube]
        (println "rendering cube...")
        (dom/div #js {:className "cube"}
          (when (not= nil process)
            (println "rendering cube...")
            (dom/div #js {:className "viewport"
                          :tabIndex "0"
                          :onKeyDown (cube/keyboard-controller bus)}
              (om/build controls/ui a-cube)
              (dom/div #js {:className "viewport-container"}
                (dom/div #js {:className "viewport-material"}
                  (om/build display/ui
                    (ps/output process screen)
                    {:init-state {:bus bus}}))))

            (if (= :running (:status a-cube))
              (om/build clock hz {:init-state {:bus bus}})
              (om/build inspector/ui (:process a-cube)))))))))


