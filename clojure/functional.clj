(def constant constantly)
(defn variable [name] (fn [args] (get args name)))

(defn operation [op]
  (fn [& args]
    (fn [values] (apply op (map (fn [arg] (arg values)) args)))))

(def add (operation +))
(def subtract (operation -))
(def multiply (operation *))
(def divide (operation (fn [a b] (/ (double a) (double b)))))
(def negate (operation -))
(def min (operation clojure.core/min))
(def max (operation clojure.core/max))

(def operations {'+ add, '- subtract, '* multiply, '/ divide, 'negate negate, 'min min, 'max max})

(defn parse [expression]
  (cond
    (symbol? expression) (variable (str expression))
    (number? expression) (constant expression)
    (seq? expression) (apply (get operations (first expression)) (map parse(rest expression)))))

(def parseFunction (comp parse read-string))