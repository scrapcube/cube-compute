(ns learnable.inspector
  (:require [learnable.process :as proc]
            [learnable.form :as form]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async :refer [put!]]))

(defn entries-list [log]
  (letfn [(lentry [at [type input]]
            (str at " - " type " : " input))]
    (cons
      (lentry 0 ["system" "start"])
      (map-indexed
        (fn [idx entry]
          (let [at (inc idx)] (lentry at entry)))
        (:entries log)))))

(defn inspect [item]
  (apply dom/ul #js {:className "item-inspection"}
    (map
      (fn [k v]
        (dom/li nil
          (dom/div #js {:className "key"} (str k " : "))
          (dom/div #js {:className "value"} (str v))))
      item)))

(defn selectable [plog]
  (let [length (count plog)]
    (map-indexed
      (fn [idx entry]
        [(- plog idx) entry])
      plog)))

(defn ui [process owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [interrupt]}]
      (let [{:keys [log status state]} process]
        (dom/div #js {:className "screen inspector"}
          (dom/div nil
            (form/heading "status:")
            (str status))

          (dom/div nil
            (form/heading "idx")
            (:now log))

          (dom/div nil
            (form/heading "state:")
            (inspect state))

          (dom/br nil)

          (dom/div nil
            (form/heading "log:")
            (if (proc/halted? process)
              (form/radio-group #(put! interrupt [:restore %])
                (selectable (entries-list log))
                (:now log))
              (form/ulist (entries-list log)))))))))


