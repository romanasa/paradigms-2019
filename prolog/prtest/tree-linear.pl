t(Key, Value, t(Key1, L1, R1), t(Key2, L2, R2)).

% map_get(TreeMap, Key, Value)
map_get(t(Key, Value, _, _), Key, Value).
map_get(t(K, V, L, _), Key, Value) :- Key < K, map_get(L, Key, Value).
map_get(t(K, V, _, R), Key, Value) :- Key > K, map_get(R, Key, Value).

% map_put(TreeMap, Key, Value, Result).
map_put(nil, Key, Value, t(Key, Value, nil, nil)).
map_put(t(K, _, L, R), K, Value, t(K, Value, L, R)).
map_put(t(K, V, L, R), Key, Value, Result) :-
			Key < K, map_put(L, Key, Value, R1),
			Result = t(K, V, R1, R).
map_put(t(K, V, L, R), Key, Value, Result) :-
			Key > K, map_put(R, Key, Value, R1),
			Result = t(K, V, L, R1).

% map_remove(TreeMap, Key, Result)
% map_remove(nil, Key, nil).


split(A, B, T):- length(T, N), N1 is N div 2, N2 is N - N1,
		append(A, B, L), length(A, N1), length(B, N2).	

% tree_build(ListMap, TreeMap)
tree_build([], nil) :- !.
tree_build(L, Result) :-
		split(A, [(M1, M2) | B], L), 
		tree_build(A, R1), tree_build(B, R2),
		Result = t(M1, M2, A, B).

