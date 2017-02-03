#!/bin/bash                                                                                                                                                                                                                    

list=" 34features.out 32features.out 31features.out 3564-0901features.out 3564-075025features.out 3564-0505features.out 3562-0901features.out 3562-075025features.out 3562-0505features.out "

for f in $list
  do
    trec_eval -q -m all_trec qrels.all run-$f > results-$f-perquery

    for i in `seq 50`
      do
        grep '^ndcg_cut_5 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> ndcg_cut_5.results-perquery-$f
	grep '^ndcg_cut_10 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> ndcg_cut_10.results-perquery-$f

#        grep '^ndcg_cut_5 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> ndcg_cut_5.results-query$i
#	grep '^ndcg_cut_10 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> ndcg_cut_10.results-query$i

#	grep '^P_5 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> P_5.results-query$i
#	grep '^P_10 '  results-$f-perquery | awk {'print $2" "$3'} | grep '^'$i' ' | awk {'print $2'} >> P_10.results-query$i
      done
  done
