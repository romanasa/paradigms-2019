(defn equal-len? [& vs] (apply == (mapv count vs)))
(defn numbers? [& ns] (every? number? ns))

(defn shape? [v] (and (seq? v) (apply numbers? v)))

(defn vector-1d? [v] (and (vector? v) (apply numbers? v)))
(defn vectors-1d? [& vs] (every? vector-1d? vs))

(defn matrix? [m] (and (vector? m) (every? vectors-1d? m) (apply equal-len? m)))
(defn matrices? [& ms] (every? matrix? ms))

(defn equal-cols? [& ms] (apply == (mapv (fn [x] (count (x 0))) ms)))

(defn suffix? [a b] (and (shape? a) (shape? b) (= (drop (- (count b) (count a)) b) a)))

(defn tensor? [t]
  (if (or (number? t) (vector-1d? t)) true
                                      (and (vector? t) (apply equal-len? t) (every? tensor? t)))
  )
(defn tensors? [& ts] (every? tensor? ts))

(defn apply-op [op predicate?] (fn [& vs]
                      {:pre [(apply predicate? vs) (apply equal-len? vs)]}
                      (apply mapv op vs)))

(def v+ (apply-op + vectors-1d?))
(def v- (apply-op - vectors-1d?))
(def v* (apply-op * vectors-1d?))

(defn scalar [& vs]
  {:pre [(apply vectors-1d? vs) (apply equal-len? vs)]}
  (apply + (apply v* vs)))

(defn det [a b c d] (- (* a d) (* b c)))

(defn vect
  ([v] {:pre [(vector-1d? v)]} v)
  ([v u]
   {:pre [(vectors-1d? v u) (== (count v) 3) (equal-len? v u)]}
   [ (det (v 1) (v 2) (u 1) (u 2))
           (- (det (v 0) (v 2) (u 0) (u 2)))
           (det (v 0) (v 1) (u 0) (u 1))]
    )
  ([v u & vs]
   {:pre [(apply vectors-1d? v u vs) (apply equal-len? v u vs) (== (count v) 3)]}
   (reduce vect (vect v u) vs))
  )

(def m+ (apply-op v+ matrices?))
(def m- (apply-op v- matrices?))
(def m* (apply-op v* matrices?))

(defn t*s [op predicate?]
  (fn *s
    ([t] {:pre [(predicate? t)]} t)
    ([t s]
     {:pre [(predicate? t) (number? s)]}
     (mapv (fn [elem] (op elem s)) t))
    ([t s & ss]
     {:pre [(predicate? t) (apply numbers? s ss)]}
     (reduce *s (*s t s) ss))
    ))

(def v*s (t*s * vector-1d?))
(def m*s (t*s v*s matrix?))

(defn m*v
  ([m] {:pre [matrix? m]} m)
  ([m v]
   {:pre [(matrix? m) (vector-1d? v) (== (count (m 0)) (count v))]}
   (mapv (fn [row] (scalar row v)) m))
  ([m v & vs]
   {:pre [(matrix? m) (vector-1d? v) (apply == (count (m 0)) (count v) (mapv count vs))]}
   (reduce m*v (m*v m v) vs))
  )

(defn transpose [m] {:pre [(matrix? m)]} (apply mapv vector m))

(defn m*m
  ([m] {:pre [(matrix? m)]} m)
  ([m n] {:pre [(matrices? m n) (== (count (m 0)) (count n))]} (mapv (fn [row] (mapv (fn [col] (scalar row col)) (transpose n))) m))
  ([m n & ms] {:pre [(apply matrices? m n ms)]} (reduce m*m (m*m m n) ms))
  )

(defn shape [t]
  {:pre [(tensor? t)]}
  (if (number? t) ()
                  (cons (count t) (shape (t 0)))))


(defn broadcast [t s step]
  (if (== step 0) t
                  (vec (repeat (first s) (broadcast t (rest s) (dec step)))))
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
     (let [st (shape t) sw (shape w)]
       {:pre [(tensors? t w) (or (suffix? st sw) (suffix? sw st))]}
       (cond
         (suffix? st sw) ((get new-operations op) (broadcast t sw (- (count sw) (count st))) w)
         (suffix? sw st) ((get new-operations op) t (broadcast w st (- (count st) (count sw))))
         )
       ))
    ([t w & ts] {:pre [(apply tensors? t w ts)]} (reduce operation-tt (operation-tt t w) ts))
    )
  )

(def b+ (apply-tt '+))
(def b- (apply-tt '-))
(def b* (apply-tt '*))
