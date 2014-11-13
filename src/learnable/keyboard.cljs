(ns learnable.keyboard
  (:require
    [om.core :as om :include-macros true]
    [cljs.core.async :as async :refer [put!]]))

(defn parse-keystroke [e]
  (condp = (.-keyCode e)
         38 :key-up
         39 :key-right
         40 :key-down
         37 :key-left
         27 :key-esc
         32 :key-space
         187 :key-plus
         189 :key-minus))

(def key-bindings
  {:key-esc
     [:halt]
   :key-space
     [:resume]
   :key-plus
     [:overclock]
   :key-minus
     [:throttle]})

(defn is-key-binding? [ks]
  (not= nil (find key-bindings ks)))

(defn load-key-binding [ks]
  (last (find key-bindings ks)))

(defn controller [bus interrupt]
  (fn [e]
    (let [ks (parse-keystroke e)]
      (if (is-key-binding? ks)
        (put! interrupt (load-key-binding ks))
        (put! bus [:keyboard ks])))))
