(ns learnable.mouse)

(defn node-offset [node]
  [(.-offsetLeft node) (.-offsetTop node)])

(defn mouse-offset [e]
  [(.-pageX e) (.-pageY e)])

(defn controller [screen owner]
  (let [px (:px screen)
        bus (om/get-state owner :bus)
        [sx sy] (node-offset (om/get-node owner))
        [mx my] (mouse-offset e)]
    (fn [e]
      (put! bus
            [:mouse
             [(Math/floor (/ (- mx sx) px))
              (Math/floor (/ (- my sy) px))]]))))
