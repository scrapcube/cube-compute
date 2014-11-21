(ns learnable.components.controls
  (:require [learnable.cube.core :as cube]
            [learnable.cube.process :as proc]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn button [text action active?]
  (dom/a
    #js {:className
           (str "button" (if (active?) "active" ""))
         :href "#"
         :onClick (if (active?)
                    (constantly nil)
                    (fn [_] (action)))}
      text))

(defn ui [a-cube owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "cube-control toolbar"}
        (dom/div #js {:className "toolgroup"}
          (button "Decrease Clock Speed"
                  (fn [] (om/transact! a-cube :hz cube/throttle))
                  (constantly false))
          (dom/span #js {:className "clock-speed"}
            (:hz a-cube))
          (button "Increase Clock Speed"
                  (fn [] (om/transact! a-cube :hz cube/overclock))
                  (constantly false))


        (dom/div #js {:className "toolgroup"}
          (button "Play"
                  (fn [] (om/transact! a-cube cube/resume))
                  (fn [] (= (get-in a-cube [:process :status]) :running)))
          (button "Pause"
                  (fn [] (om/transact! a-cube cube/halt))
                  (fn [] (= (get-in a-cube [:process :status]) :halted)))))))))
