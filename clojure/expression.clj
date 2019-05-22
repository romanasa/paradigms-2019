(defn proto-get [obj key]
  (cond
    (contains? obj key) (obj key)
    (contains? obj :prototype) (proto-get (obj :prototype) key)
    :else nil))

(defn proto-call [this key & args] (apply (proto-get this key) (cons this args)))

(defn field [key] (fn [this] (proto-get this key)))
(defn method [key] (fn [this & args] (apply proto-call this key args)))
(def _evaluate (method :evaluate))
(def _toString (method :toString))
(def _diff (method :diff))
(def _objects (field :objects))

(defn constructor [ctor prototype]
  (fn [& args]
    (let [this (apply ctor {:prototype prototype} args)]
      (fn [key & args] (apply key this args)))))


(defn ConstantCtor [this value] (assoc this :value value))
(def ConstantPrototype
  (let [value (field :value)]
    {:evaluate (fn [this _] (value this))
     :toString (fn [this] (format "%.1f" (value this)))
     }))
(def Constant (constructor ConstantCtor ConstantPrototype))
(def ZERO (Constant 0))
(def ONE (Constant 1))

(def ConstantPrototype (assoc ConstantPrototype :diff (constantly ZERO)))
(def Constant (constructor ConstantCtor ConstantPrototype))

(defn VariableCtor [this name] (assoc this :name name))
(def VariablePrototype
  (let [name (field :name)]
    {:evaluate (fn [this args] (get args (name this)))
     :toString (fn [this] (name this))
     :diff     (fn [this args] (if (= args (name this)) ONE ZERO))
     }))
(def Variable (constructor VariableCtor VariablePrototype))


(defn OperationCtor [this f name diffObject & objects]
  (assoc this :f f :name name :diffObject diffObject :objects objects))

(def OperationPrototype
  {:evaluate
   (fn [this args]
     (apply ((field :f) this) (mapv (fn [object] (object _evaluate args)) (_objects this)))
     )
   :toString
   (fn [this]
     (str
       "(" ((field :name) this) " "
       (clojure.string/join " " (mapv (fn [object] (object _toString)) (_objects this)))
       ")"
       )
     )
   :diff
   (fn [this args]
     (apply ((field :diffObject) this) (concat (_objects this) (map (fn [object] (object _diff args)) (_objects this))))
     )
   })
(def Operation (constructor OperationCtor OperationPrototype))

(defn NewOperation [op name diffObject] (fn [& args] (apply Operation op name diffObject args)))
(def Add (NewOperation + "+" (fn [_ _ da db] (Add da db))))
(def Subtract (NewOperation - "-" (fn [_ _ da db] (Subtract da db))))
(def Multiply (NewOperation * "*" (fn [a b da db] (Add (Multiply a db) (Multiply b da)))))
(def Divide (NewOperation (fn [a b] (/ (double a) (double b))) "/"
                          (fn [a b da db] (Divide (Subtract (Multiply da b) (Multiply db a)) (Multiply b b)))))
(def Negate (NewOperation - "negate" (fn [_ da] (Negate da))))
(def Square (NewOperation (fn [x] (* x x)) "square" (fn [a da] (Multiply (Constant 2) a da))))
(def Sqrt (NewOperation (fn [x] (Math/sqrt (Math/abs x))) "sqrt"
                        (fn [a da] (Divide (Multiply a da)
                                           (Multiply (Constant 2) (Sqrt (Multiply a a a)))))))


(defn evaluate [expression args] (expression _evaluate args))
(defn toString [expression] (expression _toString))
(defn diff [expression args] (expression _diff args))

(def operations {'+ Add, '- Subtract, '* Multiply, '/ Divide, 'negate Negate, 'square Square, 'sqrt Sqrt})

(defn parse [expression]
  (cond
    (symbol? expression) (Variable (str expression))
    (number? expression) (Constant expression)
    (seq? expression) (apply (get operations (first expression)) (map parse (rest expression)))
    ))

(def parseObject (comp parse read-string))
