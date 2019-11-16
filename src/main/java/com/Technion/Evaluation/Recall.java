package com.Technion.Evaluation;

import java.util.List;
import java.util.Map;

public class Recall {


    /**
     * Compute the recall at k.
     * @param ranking list of query in the ranking; should be ordered by
     *  relevance , identifiers that do not appear in the qrels are assigned 0
     * @param qrel query with relevant judgments (Non empty!).
     * @param k - Precision at k (@k)
     * @return Recall at K
     */
    public static double
    computeRecallAtK(List<String> ranking, Map<String, Integer> qrel, int k) {

        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - Recall");
        Utils.ifFalseCrash(k > 0, "k should be > 0");

        double recall = 0.0;
        int tp=0;
        int relevant = 0;

        for (int relJudg : qrel.values()) {
            if (relJudg > 0) {
                relevant++;
            }
        }

        for (int i=0; i<Math.min(ranking.size(), k); i++) {
            if (qrel.containsKey(ranking.get(i)) ) {
                if (qrel.get(ranking.get(i)) > 0) {
                    tp++;//if true, the document is relevant
                }
            }
        }
        recall = (double)tp / (double)relevant;
        return recall;
    }



}
