% t(Key, Value, L, R)

% map_get(TreeMap, Key, Value)
map_get(t(K, V, _, _), K, V).
map_get(t(K, V, L, R), Key, Value) :- (Key < K, map_get(L, Key, Value); Key > K, map_get(R, Key, Value)).

split(A, B, T):-
        length(T, N), N1 is N / 2, N2 is N - N1,
        length(A, N1), length(B, N2), append(A, B, T).

% tree_build(ListMap, TreeMap)
tree_build([], nil).
tree_build(L, t(M1, M2, R1, R2)) :- split(A, [(M1, M2) | B], L), tree_build(A, R1), tree_build(B, R2).
