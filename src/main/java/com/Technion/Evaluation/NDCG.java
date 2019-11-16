package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NDCG {

    public static double computeNdcgAtK (List<String> ranking, Map<String,Integer> qrel, int k){

        Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - NDCG");
        Utils.ifFalseCrash(k>0, "k should be > 0");

        double dcg = dcgAtK(ranking,qrel,k);
        double iDcg = idealDcgAtK (qrel,k);
        if (iDcg == 0){
            return 0.0;
        }
        return dcg/iDcg;
    }

    /**
     * Compute DCG@K
     * @param ranking
     * @param qrel
     * @param k
     * @return
     */
    private static double dcgAtK(List<String> ranking, Map<String, Integer> qrel, int k) {

        double dcg =0.0;
        for (int i=0; i<Math.min(ranking.size(),k); i++){
            if (qrel.containsKey(ranking.get(i)) ){
                int rel = qrel.get(ranking.get(i));
                if (rel > 0){
                    //dcg+= (double)rel / ( (Math.log((i+1)+1))/Math.log(2));
                    dcg+= (Math.pow(2,rel)-1) / ( (Math.log((i+1)+1))/Math.log(2));
                }
            }
        }
        return dcg;
    }

    private static double idealDcgAtK(Map<String, Integer> qrel, int k) {
        Map<String,Double> rankedDocMap = new HashMap<String,Double>();
        for (Map.Entry<String,Integer> entry : qrel.entrySet()){
            rankedDocMap.put(entry.getKey(),(double)entry.getValue());
        }
        //order map keys by value (relJudg)
        List<String> idealRanking = new ArrayList<String>(
                ScoreFileReader.sortMapByValues(rankedDocMap,ScoreFileReader.DESCENDING).keySet());

        return dcgAtK(idealRanking,qrel,k);
    }
}
