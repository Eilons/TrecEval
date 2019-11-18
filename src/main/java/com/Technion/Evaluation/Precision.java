package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.List;
import java.util.Map;

public class Precision {

    /**
     * Compute the precision at k.
     *
     * @param ranking list of query in the ranking; should be ordered by
     *    relevance , identifiers that do not appear in the qrels are assigned 0
     * @param qrel query with relevant judgments (Non empty!).
     * @param k - Precision at k (@k)
     * @return precision at k
     */
    public static double computePrecisionAtK(List<String> ranking, Map<String, Integer> qrel, int k) {
        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - Precision");
        Utils.ifFalseCrash(k > 0, "k should be > 0");

        double precision = 0.0;
        int tp=0;
        int i;
        for (i=0; i<Math.min(ranking.size(), k); i++) {
            if (qrel.containsKey(ranking.get(i)) ) {
                if (qrel.get(ranking.get(i)) > 0){
                    tp++;         //if true, the document is relevant
                }
            }
        }
        precision = (double)tp / (double)k;
        return precision;
    }

    /**
     * Compute the precision at k for k=[1,threshold].
     * @param ranking list of documents in the ranking; should be ordered by relevance.
     * Identifiers that do not appear in the qrels are assigned 0 (not relevant)
     * @param qrel query with relevant judgments.
     * @param maxK max value for k
     * @return array of precisions at k for k=[1, threshold]
     */ // TODO - TEST
    public static  double[]
    computePrecisions(List<String> ranking, Map<String, Integer> qrel, int maxK) {
        Utils.ifFalseCrash(maxK > 0, "The threshold should be > 0");
        double[] precisions = new double[maxK];
        int tp =0;

        for (int i=0; i< maxK; i++) {
            precisions[i] = computePrecisionAtK(ranking,qrel,i+1);
        }
        return precisions;
    }

    /**
     * Use {@code computePrecisions(ranking, gold)} to consider the default
     * threshold of 5.
     * The minimum between threshold and size of ranking is considered as the max k.
     * @param ranking list of documents in the ranking; should be ordered by relevance.
     * Identifiers that do not appear in the qrels are assigned 0 (not relevant)
     * @param qrel query with relevant judgments.
     * @return  array of precisions at k for k=[1, threshold]
     */
    public static  double[] computePrecisions(List<String> ranking, Map<String, Integer> qrel) {
        return computePrecisions(ranking, qrel, 5);
    }
}
