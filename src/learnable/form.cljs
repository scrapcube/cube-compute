(ns learnable.form
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn heading [text]
  (dom/div #js {:className "heading"} text))

(defn ulist [items]
  (apply
    dom/ul
    #js {:className "ulist"}
    (map (fn [item]
           (dom/li #js {:key item}
                   (str " * " item)))
         items)))

(defn radio-group [fselect options radio-value]
  (apply
    dom/ul
    #js {:className "radio-group"}
    (map (fn [option]
           (let [[value label] option]
             (dom/li
                #js {:key value}
                (let [selected? (= value radio-value)
                      mark (if selected? "[X] " "[ ] ")]
                   (dom/a #js {:onClick (fn [_] (fselect value))}
                          (str mark label))))))
         options)))

