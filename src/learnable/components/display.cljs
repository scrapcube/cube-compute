(ns learnable.components.display
  (:require
    [learnable.cube.core :as cube]
    [learnable.cube.graphix :as graphix]
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(enable-console-print!)

; Style Helpers
; =============
; These functions provide appropriate styles for various graphics objects.
;
; The motivation for separating these functions are readability and the
; option for changing these definitions to multimethods that dispatch on
; the :type property of the graphics object. This option is desirable because
; we can support a greater number of object types.

(defn box-style [graphic]
  (let [[x y] (:offset graphic)
        [width height] (:dimensions graphic)
        color (:color graphic)]
    #js {:position "absolute"
         :left (str x "px")
         :top (str y "px")
         :width (str width "px")
         :height (str height "px")
         :background (if color (name color) "#FFF")}))

; # Rendering Functions
; =====================
; The function `render-graphic` is used to render leaf nodes of the graphics
; tree, whereas `render-surface` is used to render non-leaf nodes of the
; graphics tree.
;
; When rendering a surface, we must also render its children. We do this by
; calling `render-graphic` or `render-surface`, depending on value of applying
;
;   (is-surface? child)
;
; The function `is-surface?` dispatches on the `:type` property of the child.
;
; Currently,
;
; (is-surface obj) is true when
;   (or (= :canvas (:type obj))
;       (= :grid (:type obj)))

(defn render-graphic [graphic]
  (let [{:keys [id etype offset]} graphic]
    (dom/div #js {:style (box-style graphic)
                  :key (str (name etype) (first offset) (last offset))}
             "")))

(defn render-surface [surface mouse transforms]
  (let [{:keys [etype id transform items offset dimensions]} surface
        transforms-prime (cons transform transforms)]
    (apply
      dom/div
      #js {:style (box-style surface)
           :key (str "surface" (name id))
           :onClick (mouse (fn [point]
                              [id ((apply comp transforms-prime) point)]  ))}
      (map
        (fn [item]
          (if (graphix/is-surface? item)
            (render-surface item mouse transforms-prime)
            (render-graphic item)))
        items))))

; # Main Component
; ================
; Establishes the root page offset and begins an in-order rendering traversal
; of the graphics tree. Our rendering process assumes that `frame` is a surface.

(defn examine [obj depth]
  (if (seq? obj)
    (str (map #(examine % (inc depth)) obj) "\n")
    (str (repeat depth " ") obj)))

(defn ui [frame owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (om/set-state! owner
        :page-offset
          (let [domn (om/get-node owner)]
            (list (.-offsetLeft domn) (.-offsetTop domn)))))

    om/IRenderState
    (render-state [_ {:keys [page-offset bus]}]
      (println (str page-offset))
      (println (examine frame 0))
      (println (str (:etype frame)))
      (dom/div
        #js {:style #js {:position "relative"
                         :width (first (:dimensions frame))
                         :height (last (:dimensions frame))}
             :key {}}
        (render-surface frame (cube/mouse-controller bus) (list #(map - % page-offset)))))))
