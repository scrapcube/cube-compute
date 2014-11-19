(ns learnable.display
  (:require
    [om.core :as om :include-macros true]
    [om.dom :as dom :include-macros true]))

(enable-console-print!)

(defn box-css [graphic]
  (let [[x y] (:offset graphic)
        [width height] (:dimensions graphic)]
    {:position "absolute"
     :left (str x "px")
     :top (str y "px")
     :width (str width "px")
     :height (str height "px")}))

(defn color-css [graphic]
  {:background (name (:color graphic))})

; Style Helpers
; =============
; These functions provide appropriate styles for various graphics objects.
;
; The motivation for separating these functions are readability and the
; option for changing these definitions to multimethods that dispatch on
; the :type property of the graphics object. This option is desirable because
; we can support a greater number of object types.

(defn graphic-style [graphic]
  (merge
    (box-css graphic)
    (when (:color graphic) (color-css graphic))))

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
  (apply
    dom/div
    #js (graphic-style graphic)
    (graphic-contents graphic)))

(defn render-surface [surface mouse transforms]
  (let [{:keys [transform items offset dimensions]} surface
        transforms-prime (cons transform transforms)]
    (apply
      dom/div
      #js {:style #js (box-css graphic)
           :onClick (mouse (apply comp transforms-prime))}
      (map
        (fn [item]
          (if (is-surface? item)
            (render-surface item mouse transforms-prime)
            (render-graphic item)))
        items))))

; # Main Component
; ================
; Establishes the root page offset and begins an in-order rendering traversal
; of the graphics tree. Our rendering process assumes that `frame` is a surface.

(defn ui [frame owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (om/set-state! owner
        :page-offset
          (let [domn (om/get-node owner)
            (list (.-offsetLeft domn) (.-offsetTop domn))])))

    om/IRenderState
    (render-state [_ {:keys [page-offset mouse]}]
      (dom/div
        nil
        (render-surface frame mouse (list #(map - % page-offset)))))))
