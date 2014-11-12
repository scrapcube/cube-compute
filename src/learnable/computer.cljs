(ns learnable.computer
  (:require
    [learnable.display :as display]
    [learnable.clock :as clock]
    [learnable.process :as proc]
    [learnable.inspector :as inspector]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]
    [cljs.core.async :as async :refer [chan put! <!]])
  (:require-macros
    [cljs.core.async.macros :as async-mac :refer [go]]))

(enable-console-print!)

(defn assemble-grid-computer [unit dim res hz]
  {:screen (screen/grid-screen unit dim res)
   :hz hz})

(defn run-program [computer program]
  (assoc computer :process (proc/launch program (:screen computer))))

(defn ui [computer owner]
  (reify
    om/IInitState
    (init-state [_]
      {:bus (chan)
       :interrupt (chan)})

    om/IWillMount
    (will-mount [_]
      (let [interrupt (om/get-state owner :interrupt)]
        (go (loop []
          (let [[command data] (<! interrupt)]
            (condp = command
              :halt
                (om/transact! computer :process proc/halt)
              :resume
                (om/transact! computer :process proc/resume)
              :restore
                (om/transact! computer :process #(proc/restore % data))
              :overclock
                (om/transact! computer :hz clock/overclock)
              :throttle
                (om/transact! computer :hz clock/throttle))
            (recur)))))
      (let [bus (om/get-state owner :bus)]
        (go (loop []
          (let [entry (<! bus)]
            (when (proc/running? (:process @computer))
              (om/transact! computer :process #(proc/commit % entry)))
            (recur)))))

      (js/setTimeout
        (fn [] (put! (om/get-state owner :interrupt) [:resume]))
        1000))

    om/IRenderState
    (render-state [_ state]
      (let [{:keys [hz process screen]} computer]

        (dom/div nil
          (dom/div
            #js {:id "clock"}
            (om/build clock/ui
              (hz :computer)
              {:init-state state})

          (dom/div
            #js {:id "main"}
            (om/build display/ui
              (proc/output process screen)
              {:init-state state}))

          (dom/div
            #js {:id "inspector"}
            (om/build inspector/ui
              process
              {:init-state state}))))))))
