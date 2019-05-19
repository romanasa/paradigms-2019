max([X], X) :- !.
max([X | Xs], M):- max(Xs, M), M >= X.
max([X | Xs], X):- max(Xs, M), M < X.

% t(Key, Value, Sons, Max).
t(Key, Value, nil, Key):- !.
t(Key, Value, [t(_, _, _, M1), t(_, _, _, M2)], M):-
			M1 < M2, max([Key, M1, M2], M).
t(Key, Value, [t(_, _, _, M1), t(_, _, _, M2), t(_, _, _, M3)], M):- 
			M1 < M2, M2 < M3, max([Key, M1, M2, M3], M).

% map_get(TreeMap, Key, Value)
map_search(nil, _, 
map_search(T, Key, Value, T) :- T = t(Key, Value, _, _).
map_search(t(_, _, [L, R], M), Key, Value, Result):- L = t(_, _, _, M1), R = t(_, _, _, M2),
				( Key =< M1, map_search(L, Key, Value, Result); Key > M1, map_search(R, Key, Value, Result)).
				
map_search(t(_, _, [L, M, R], _), Key, Value, Result):-
		L = t(_, _, _, M1), M = t(_, _, _, M2), R = t(_, _, _, M3),
		(	
			Key =< M1, map_get(L, Key, Value, Result);
			M1 < Key, Key =< M2, map_get(M, Key, Value, Result);
			M2 < Key, Key =< M3, map_get(R, Key, Value, Result)
		).

map_get(T, Key, Value):- map_search(T, Key, Value, R).

			

% map_put(TreeMap, Key, Value, Result).
map_put(nil, Key, Value, t(Key, Value, nil, Key)).
map_put(T, Key, Value, Result):-
		map_search(T, Key, 

map_put(t(K, _, L, R), K, Value, t(K, Value, L, R)).
map_put(t(K, V, L, R), Key, Value, Result) :-
			Key < K, map_put(L, Key, Value, R1),
			Result = t(K, V, R1, R).
map_put(t(K, V, L, R), Key, Value, Result) :-
			Key > K, map_put(R, Key, Value, R1),
			Result = t(K, V, L, R1).

% map_remove(TreeMap, Key, Result)
% map_remove(nil, Key, nil).

% tree_build(ListMap, TreeMap)
tree_build([], nil).
tree_build([(H1, H2) | T], Result) :- 
		tree_build(T, R1), map_put(R1, H1, H2, Result).


