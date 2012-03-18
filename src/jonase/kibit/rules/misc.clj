(ns jonase.kibit.rules.misc
  (:use [clojure.core.logic :only [defne project pred]]))


(defn not-method? [sym]
  (not= (first (str sym)) \.))

(defne fn-call? [expr]
  ([[_ [_ . _] [fun . _]]]
     (project [fun]
       (pred fun symbol?)
       (pred fun not-method?))))

(def rules
  [;; clojure.string
   ['(apply str (interpose ?x ?y)) [] '(clojure.string/join ?x ?y)]
   ['(apply str (reverse ?x)) [] '(clojure.string/reverse ?x)]
   
   ;; mapcat
   ['(apply concat (apply map ?x ?y)) [] '(mapcat ?x ?y)]
   ['(apply concat (map ?x . ?y)) [] '(mapcat ?x . ?y)]
   
   ;; filter
   ['(filter (complement ?pred) ?coll) [] '(remove ?pred ?coll)] 
   ['(filter #(not (?pred ?x)) ?coll) [] '(remove ?pred ?coll)]
   
   ;; Unneeded anonymous functions -- see bug #16
   ['(fn ?args (?fun . ?args)) [fn-call?] '?fun]
   ['(fn* ?args (?fun . ?args)) [fn-call?] '?fun]
   
   ;; do
   ['(do ?x) [] '?x]
   
   ;; Java stuff
   ['(.toString ?x) [] '(str ?x)]
   
   ;; Threading
   ['(-> ?x ?y) [] '(?y ?x)]
   ['(->> ?x ?y) [] '(?y ?x)]
   
   ;; Other
   ['(not (= . ?args)) [] '(not= . ?args)]])

(comment
  (apply concat (apply map f (apply str (interpose \, "Hello"))))
  (filter (complement nil?) [1 2 3])

  (.toString (apply str (reverse "Hello")))
  
  (map (fn [x] (inc x)) [1 2 3])
  (map (fn [x] (.method x)) [1 2 3])
  (map #(dec %) [1 2 3])
  (map #(.method %) [1 2 3])

  )
