% t(Key, Value, Prior, L, R)
% merge(L, R, T)
merge(L, nil, L) :- !.
merge(nil, R, R).
merge(L, R, T) :- 
			L = t(KeyL, ValueL, PrL, LL, LR), R = t(KeyR, ValueR, PrR, RL, RR),
			(PrL > PrR, merge(LR, R, Result), T = t(KeyL, ValueL, PrL, LL, Result);
			PrL =< PrR, merge(L, RL, Result), T = t(KeyR, ValueR, PrR, Result, RR)).
		
% split(T, Key, <= Key, > Key)
split(nil, _, nil, nil, _).
split(T, K, Res1, Res2, Eq) :- 
			(Eq, K < Key; not Eq, K =< Key) -> 
				split(L, K, P1, P2, Eq), Res1 = P1, Res2 = t(Key, Value, Pr, P2, R);
				split(R, K, P1, P2, Eq), Res1 = t(Key, Value, Pr, L, P1), Res2 = P2.

% map_get(TreeMap, Key, Value)
map_get(t(K, V, _, _, _), K, V).
map_get(t(K, V, _, L, R), Key, Value) :- (Key < K, map_get(L, Key, Value); Key > K, map_get(R, Key, Value)).

% get_parts(T, Key, < Key, = Key, > Key)
get_parts(T, K, L, M, R) :- split(T, K, L, Tmp, fail), split(Tmp, K, M, R, true). 

% insert(T, Node, Result)
insert(T, Node, Result) :- 
    Node = t(K, _, _, _, _),
		get_parts(T, K, L, _, R),
		merge(L, Node, Tmp), merge(Tmp, R, Result).
		
% map_put(TreeMap, Key, Value, Result)
%MAXC=1000000.
max_value(1000000000).

map_put(T, K, V, R) :-
		max_value(MAXC), rand_int(MAXC, Pr),
		insert(T, t(K, V, Pr, nil, nil), R).

%map_remove(TreeMap, Key, Result)
map_remove(T, K, Result) :- get_parts(T, K, L, _, R), merge(L, R, Result).

%map_replace(Map, Key, Value, Result)
%map_replace(T, Key, Value, Result) :- replace(T, Key, Value, Result), !.

map_replace(t(K, _, Pr, L, R), K, V, t(K, V, Pr, L, R)) :- !.
map_replace(T, K, V, Result) :- T = t(Key, Value, Pr, L, R),
		 K < Key, map_replace(L, K, V, ResL), !, Result = t(Key, Value, Pr, ResL, R);
		 K > Key, map_replace(R, K, V, ResR), !, Result = t(Key, Value, Pr, L, ResR).
map_replace(T, _, _, T).

% tree_build(ListMap, TreeMap)
tree_build([], nil).
tree_build([(H1, H2) | T], Result) :- tree_build(T, R1), map_put(R1, H1, H2, Result).
