package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.List;
import java.util.Map;

public class ReciprocalRank {

    /**
     * Compute the reciprocal rank for a given ranking. It is defined as 1/rank_i
     * where rannk_i represents the first rank in which a relevant instance has been
     * positioned.
     * <br />
     *
     * @param ranking list of query in the ranking; should be ordered by
     *  relevance , identifiers that do not appear in the qrels are assigned 0
     * @param qrel query with relevant judgments (Non empty!).
     * @return  reciprocal rank (0 if none is correct)
     */
    public static double
    computeReciprocalRank(final List<String> ranking, final Map<String, Integer> qrel) {

        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty- MRR");

        double reciprocalRank = 0;
        for (int i = 0; i < ranking.size(); i++) {
            if (qrel.containsKey(ranking.get(i))) {
                if (qrel.get(ranking.get(i)) > 0) {
                    // if true, the query is relevant and we finish
                    reciprocalRank = 1.0 / (i + 1);
                    break;
                }
            }
        }
            return reciprocalRank;
    }

}
