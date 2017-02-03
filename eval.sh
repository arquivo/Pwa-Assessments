#!/bin/bash                                                                                                                                                                                                                    

list=" 34features.out 32features.out 31features.out 3564-0901features.out 3564-075025features.out 3564-0505features.out 3562-0901features.out 3562-075025features.out 3562-0505features.out "

for f in $list
  do
    grep '^TREC' $f | cut -f 2- -d " " > run-$f
    trec_eval -c -m all_trec qrels.all run-$f > results-$f

    grep '^ndcg_cut_1 '  results-$f | awk {'print $3'} >> ndcg_cut_1.results
    grep '^ndcg_cut_5 '  results-$f | awk {'print $3'} >> ndcg_cut_5.results
    grep '^ndcg_cut_10 '  results-$f | awk {'print $3'} >> ndcg_cut_10.results

    grep '^P_1 '  results-$f | awk {'print $3'} >> P_1.results
    grep '^P_5 '  results-$f | awk {'print $3'} >> P_5.results
    grep '^P_10 '  results-$f | awk {'print $3'} >> P_10.results

    grep '^num_rel_ret '  results-$f | awk {'print $3'} >> num_rel_ret.results

    grep '^success_1 '  results-$f | awk {'print $3'} >> success_1.results
    grep '^success_5 '  results-$f | awk {'print $3'} >> success_5.results
    grep '^success_10 '  results-$f | awk {'print $3'} >> success_10.results
  done

