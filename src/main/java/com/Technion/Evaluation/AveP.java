package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of Average Precision - This class will be used by 'RunEvaluation' to compute MAP
 */
public class AveP {

    /**
     * Computation of average precision
     * @param ranking list of queries, ordered by relevance
     * @param qrel query with relevant judgments (Non empty!).
     * @return average precision
     */
    public static double
    computeAveragePrecision(List<String> ranking, Map<String, Integer> qrel) {

        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - AP");

        int countRelevant = getRelDocumentCount(qrel);
        double avPrec = 0.0;

        List<Integer> relPos = getRelPos(ranking,qrel);
        for (int i=0; i < relPos.size(); i++) {

            avPrec += (i+1)/ (double)relPos.get(i);
        }
        if (countRelevant==0) {
            Utils.ifFalseCrash(avPrec==0, "AP is not zero: " + avPrec +
                    " but there is no relevant query in qrels");
            return 0;
        }
        else {
            return avPrec*(1.0/(double)countRelevant);
        }
    }

    /**
     *
     * @param ranking
     * @param qrel
     * @return relevant positions in res file of relevant queries
     */
    private static List<Integer> getRelPos(List<String> ranking,
                                           Map<String,Integer> qrel) {

        List<Integer> relPositionList = new ArrayList<Integer>();
        for (int i=0; i< ranking.size(); i++){

            if (qrel.containsKey(ranking.get(i))){
                if (qrel.get(ranking.get(i)) > 0){
                    relPositionList.add(i+1);
                }
            }
        }
        return relPositionList;
    }

    /**
     *
     * @param qrel
     * @return the number of relevant document in qrel file for some topic
     */
    private static int getRelDocumentCount(Map<String,Integer> qrel) {

        int count =0;
        for (int relJudg : qrel.values()){
            if (relJudg > 0)
                count++;
        }
        return count;
    }


}
