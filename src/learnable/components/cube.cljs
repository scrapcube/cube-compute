(ns learnable.components.cube
  (:require [learnable.cube.process :as proc]
            [learnable.cube.core :as cube]
            [learnable.components.display :as display]
            [learnable.components.info :as info]
            [learnable.components.inspector :as inspector]
            [learnable.components.timeline :as timeline]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

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
    (render-state [_ _] (dom/span #js {:className "hidden"} ""))))

(defn ui [a-cube owner]
  (reify
    IWillMount
    (will-mount [_]
      (let [bus (om/get-state owner :bus)]
        (go (loop []
          (let [entry (<! bus)]
            (when (= :running (get-in a-cube [:process :status]))
              (om/transact! a-cube :process #(proc/commit % entry))))))))

    IRenderState
    (render-state [_ {:keys [bus]}]
      (let [{:keys [process screen hz]} a-cube]
        (dom/div nil

          (dom/div
            #js {:id "cube-screen"
                 :position "relative"
                 :style
                    #js {:width (str (:width screen) "px")
                         :height (str (:height screen) "px")}
                 :onKeyDown (cube/keyboard-controller bus)}
            (om/build display/ui
              (proc/output process screen)
              {:init-state
                {:mouse (cube/mouse-controller bus)}}))

          (dom/div #js {:id "cube-interface"}
            (om/build controls/ui process)
            (if (= :running (:status process))
              (om/build clock hz)
              (do
                (om/build info/ui a-cube)
                (om/build inspector/ui process)
                (om/build timeline/ui process)))))))))


