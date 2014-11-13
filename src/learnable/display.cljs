(ns learnable.display
  (:require
    [learnable.keyboard :as keyboard]
    [learnable.mouse :as mouse]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(defn screen-pixel-size [dim res]
  (min (/ (first dim) (first res))
       (/ (last dim) (last res))))

(defn screen-offset [dim res px]
  [(/ (- (first dim) (* first res px)) 2)
   (/ (- (last dim) (* last res px)) 2)])

(defn grid-screen [unit dim res]
  (let [px (screen-pixel-size dim res)]
    {:unit unit
     :dimension dim
     :resolution res
     :px px
     :offset (screen-offset dim res px)
     :items []}))

(defn draw-pixel [screen point color]
  (let [pixel [:pixel point color]]
    (update-in screen [:items] #(conj % pixel))))

(defn color-class [color]
  (condp = color
    :red "red"
    :green "green"
    :blue "blue"
    :yellow "yellow"))

(defmulti render-screen-item (fn [item _] (first item)))

(defn css-measure [unit value] (str value unit))

(defmethod render-screen-item :pixel [pixel screen]
  (let [[_ [x y] color] pixel
        measure (partial css-measure (:unit screen))
        [x-offset y-offset] (:offset screen)
        px (:px screen)]
    (dom/div
      #js {:key (str pixel)
           :className (str "pixel " (color-class color))
           :style
             {:display "absolute"
              :top (measure (+ y-offset (* y px)))
              :left (measure (+ x-offset (* x px)))
              :width (measure px)
              :height (measure px)}}
      "")))

(defn ui [screen owner]
  (reify
    om/IRenderState
    (render-state [_ {:keys [bus interrupt]}]
      (let [{:keys [dimension unit]} screen
            [screen-width screen-height] dimension]
        (apply
          dom/div
           #js {:tabIndex "0"
                :className "screen"
                :onClick (mouse/controller screen bus)
                :onKeyboard (keyboard/controller bus interrupt)
                :style
                  {:width (css-measure unit screen-width)
                   :height (css-measure unit screen-height)}}
           (map #(render-screen-item % screen) (:items screen)))))))
