(ns learnable.components.cube
  (:require [learnable.cube.process :as proc]
            [learnable.cube.core :as cube]
            [learnable.components.display :as display]
            [learnable.components.info :as info]
            [learnable.components.inspector :as inspector]
            [learnable.components.timeline :as timeline]
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

(defn inspection-ui [a-cube owner]
  (reify
    om/IRender
      (render [_]
        (dom/div
          #js {:className "inspector"}
          (om/build timeline/ui (:process a-cube))
          (om/build inspector/ui (:process a-cube))))))

(defn ui [a-cube owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [bus (om/get-state owner :bus)]
        (go (loop []
          (let [entry (<! bus)]
            (om/transact! a-cube :process
              (fn [process]
                (if (= :running (:status process))
                  (proc/commit process entry) ;; [:mouse [:game (0 0)]]
                  process)))
            (recur))))))

    om/IRenderState
    (render-state [_ {:keys [bus]}]
      (let [{:keys [process screen hz]} a-cube]
        (dom/div #js {:className "cube"}

          (dom/div
            #js {:className "display"
                 :tabIndex  "0"
                 :onKeyDown (cube/keyboard-controller bus)}
            (om/build display/ui
                      (proc/output process screen)
                      {:init-state{:bus bus}}))

          (dom/div #js {:className "interface"}
            (om/build controls/ui a-cube)
            (if (= :running (:status process))
              (om/build clock hz {:init-state {:bus bus}})
              (om/build inspection-ui a-cube))))))))


