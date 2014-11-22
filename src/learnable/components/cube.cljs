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
            (om/transact! a-cube :process
              (fn [process]
                (if (= :running (:status @a-cube))
                  (ps/commit process entry)
                  process)))
            (recur))))))

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (let [{:keys [process screen hz]} a-cube]
        (dom/div #js {:className "cube"}

          (dom/div
            #js {:className "display"}
            (om/build display/ui
                      (ps/output process screen)
                      {:init-state {:bus bus}}))

          (dom/div #js {:className "interface"}
            (om/build controls/ui a-cube)
            (if (= :running (:status a-cube))
              (om/build clock hz {:init-state {:bus bus}})
              (om/build inspector/ui (:process a-cube)))))))))


