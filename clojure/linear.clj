(defn equal-len? [& vs] (apply == (mapv count vs)))

(defn numbers? [& vs] (every? number? vs))

(defn vector-1d? [v] (and (vector? v) (every? number? v)))
(defn vectors-1d? [& vs] (every? vector-1d? vs))

(defn matrix? [m] (and (vector? m) (not (empty? m)) (every? vectors-1d? m) (apply equal-len? m)))
(defn matrices? [& ms] (every? matrix? ms))

(defn equal-types? [& ts] (or (every? vectors-1d? ts) (every? matrices? ts)))
(defn suffix? [a b] (and (vectors-1d? a b) (<= (count a) (count b)) (= (drop (- (count b) (count a)) b) a)))

(defn tensor? [t]
  (if (or (number? t) (vector-1d? t)) true
                                      (and (vector? t) (apply equal-len? t) (every? tensor? t)))
  )
(defn tensors? [& ts] (every? tensor? ts))

(defn apply-vv [op] (fn [& vs]
                      {:pre [(apply vectors-1d? vs) (apply equal-len? vs)]}
                      (apply mapv op vs)))

(defn apply-mm [op] (fn [& ms]
                      {:pre [(apply matrices? ms) (apply equal-len? ms)]}
                      (apply mapv op ms)))

(def v+ (apply-vv +))
(def v- (apply-vv -))
(def v* (apply-vv *))

(defn scalar [& vs]
  {:pre [(apply vectors-1d? vs)]}
  (apply + (apply v* vs)))

(defn det [a b c d] (- (* a d) (* b c)))

(defn vect
  ([v] {:pre [(vector-1d? v)]} v)
  ([v u]
   {:pre [(vectors-1d? v u) (== (count v) 3) (equal-len? v u)]}
   (vector (det (v 1) (v 2) (u 1) (u 2))
           (- (det (v 0) (v 2) (u 0) (u 2)))
           (det (v 0) (v 1) (u 0) (u 1))
           ))
  ([v u & vs]
   (apply vect (vect v u) vs))
  )

(def m+ (apply-mm v+))
(def m- (apply-mm v-))
(def m* (apply-mm v*))

(defn t*s [op] (fn apply-vs
                 ([t] {:pre [(or (vector-1d? t) (matrix? t))]} t)
                 ([t s]
                  {:pre [(or (vector-1d? t) (matrix? t)) (number? s)]}
                  (mapv (fn [x] (op x s)) t))
                 ([t s & ss]
                  (apply apply-vs (apply-vs t s) ss))
                 ))

(def v*s (t*s *))
(def m*s (t*s v*s))

(defn m*v
  ([m] {:pre [matrix? m]} m)
  ([m v]
   {:pre [(matrix? m) (vector-1d? v)]}
   (mapv (fn [row] (scalar row v)) m)
    )
  ([m v & vs] (apply m*v (m*v m v) vs))
  )

(defn transpose [m] {:pre [(matrix? m)]} (apply mapv vector m))

(defn m*m
  ([m] {:pre [(matrix? m)]} m)
  ([m n] {:pre [(matrices? m n) (== (count (m 0)) (count n))]} (mapv (fn [row] (mapv (fn [col] (scalar row col)) (transpose n))) m))
  ([m n & ms] (apply m*m (m*m m n) ms))
  )

(defn shape [t]
  {:pre [(tensor? t)]}
  (if (number? t) []
                  (vec (cons (count t) (shape (t 0))))))

(defn broadcast [t s]
  {:pre [(tensor? t) (vector-1d? s) (suffix? (shape t) s)]}
  (if (equal-len? (shape t) s) t
                               (vec (repeat (first s) (broadcast t (vec (rest s))))))
  )

(defn new-operation [op]
  (fn
    ([t]
     {:pre [(tensor? t)]}
     (if (number? t) (op t)
                     (mapv (new-operation op) t)))
    ([t s]
     {:pre [(tensors? t s) (= (shape t) (shape s))]}
     (if (number? t) (op t s)
                     (mapv (new-operation op) t s)))
    ))

(def new-operations {'+ (new-operation +), '- (new-operation -), '* (new-operation *)})

(defn apply-tt [op]
  (fn operation-tt
    ([t]
     {:pre [(tensor? t)]}
     ((get new-operations op) t))
    ([t w]
     {:pre [(tensors? t w) (or (suffix? (shape t) (shape w)) (suffix? (shape w) (shape t)))]}
     (cond
       (suffix? (shape t) (shape w)) ((get new-operations op) (broadcast t (shape w)) w)
       (suffix? (shape w) (shape t)) ((get new-operations op) t (broadcast w (shape t)))
       ))
    ([t w & ts] (apply operation-tt (operation-tt t w) ts))
    )
  )

(def b+ (apply-tt '+))
(def b- (apply-tt '-))
(def b* (apply-tt '*))
