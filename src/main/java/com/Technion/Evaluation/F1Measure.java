package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.List;
import java.util.Map;

public class F1Measure {

    /**
     * Computation of F1 measure
     * @param ranking list of queries, ordered by relevance
     * @param qrel query with relevant judgments (Non empty!).
     * @return F1 measure: 2* (precision*recall)/(precision+recall)
     */
    public static double
    computeF1Measure(List<String> ranking, Map<String, Integer> qrel, int k) {

        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - F1");

        double precision = Precision.computePrecisionAtK(ranking,qrel,k);
        double recall = Recall.computeRecallAtK(ranking,qrel,k);

        if (precision + recall == 0) return 0;

        return 2.0*(precision*recall)/ (precision+recall);
    }

    public static double
    computeF1Measure(double precision, double recall) {

        if (precision + recall == 0) return 0;

        return 2.0*(precision*recall)/ (precision+recall);
    }
}
