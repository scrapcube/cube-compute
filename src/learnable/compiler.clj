(ns learnable.compiler
  (:require
    [clojure.tools.reader :as reader]
    [clojure.tools.reader.reader-types :as readers]
    [cljs.analyzer :as ana]
    [cljs.compiler :as compiler]
    [cljs.env :as env]))

;; A helper to emit ClojureScript compiled to Javascript as a string
(defn emit-str [ast]
  (with-out-str (compiler/emit ast)))

;; A helper which allows us to read ClojureScript source from a string instead
;; of files.
(defn string-reader [s]
  (clojure.lang.LineNumberingPushbackReader. (java.io.StringReader. s)))

;; A helper that takes a stream and returns a lazy sequence of read forms.
(defn forms-seq [stream]
  (let [rdr (readers/indexing-push-back-reader stream 1)
        forms-seq* (fn forms-seq* []
                     (lazy-seq
                      (if-let [form (reader/read rdr nil nil)]
                        (cons form (forms-seq*)))))]
    (forms-seq*)))


(defn make-env [user-ns]
  {:ns     {:name user-ns}
   :locals {}})

(defn compile-form [code user-ns]
  (let [code-stream (string-reader code)
        form-seq    (forms-seq code-stream)
        user-env    (make-env user-ns)]
    (reduce
      (fn [output form]
        (str output (emit-str (ana/analyze user-env form))))
      ""
      form-seq)))
