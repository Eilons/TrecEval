package com.Technion.Evaluation;

import com.Technion.Utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Rouge {

    private static String SEP = "__";


    /**
     * Average over results attained for each query proposed by the system for each article
     * @param sysSuggestedQueries - the system suggested query
     * @param qrel
     * @param ngram - 1: ROUGE-1, 2: ROUGE-2, ...
     * @return
     */ // paper of Keikha&Croft: "Evaluating Answer Passages using Summarization Measures" - says that maximum is a better
    // option choice for passage evaluation
    public static double computeRougeMaxF1 (List<String> sysSuggestedQueries, Map<String, Integer> qrel, int ngram) {

        double avgMaxF1 = 0;
        for (String sysQuery : sysSuggestedQueries) {
            avgMaxF1+= computeRougeMaxF1(sysQuery,qrel,ngram);
        }

        return avgMaxF1 / (double)sysSuggestedQueries.size();

    }


    /**
     *
     * @param sysQuery - the system suggested query
     * @param qrel
     * @param ngram - 1: ROUGE-1, 2: ROUGE-2, ...
     * @return
     */ // paper of Keikha&Croft: "Evaluating Answer Passages using Summarization Measures" - says that maximum is a better
        // option choice for passage evaluation
    private static double computeRougeMaxF1 (String sysQuery, Map<String, Integer> qrel, int ngram) {

        double maxF1 = 0;
        for (Map.Entry<String,Integer> entry : qrel.entrySet()) {

            int overlap = 0;
            if (entry.getValue() > 0) {
                List<String> sysNgram = getNGramTokens(ngram, Utils.splitBySpace(sysQuery)); // the system suggested query
                List<String> refNgram = getNGramTokens(ngram,Utils.splitBySpace(entry.getKey())); // the reference query (from qrel)

                for (String sysToken : sysNgram) {
                    if (refNgram.contains(sysToken)) {
                        refNgram.remove(sysToken);
                        overlap++;
                    }
                }
                double f1Score = computeF1Score (overlap, sysNgram, getNGramTokens(ngram,Utils.splitBySpace(entry.getKey())) );
                maxF1 = f1Score > maxF1? f1Score : maxF1;
            }
        }
        return maxF1;

    }

    /**
     * Compute the f1 score given the overlap status
     * @param overlap
     * @param sysNgram
     * @param refNGram
     * @return
     */
    private static double computeF1Score(int overlap, List<String> sysNgram, List<String> refNGram) {

        double rougePrecision = (double)overlap / (double)sysNgram.size();
        double rougeRecall = (double)overlap / (double)refNGram.size();
        return F1Measure.computeF1Measure(rougePrecision, rougeRecall);
    }


    /**
     * The query from the qrel files + the query suggested by the system are in tokenized form and without
     * punctuation marks and stopwords
     * @param ngram
     * @param queryTerms
     * @return
     */
    private static List<String> getNGramTokens (int ngram, String[] queryTerms) {

        Utils.ifFalseCrash(ngram>0, "ngram must be greater than 0 - rouge");
        List<String> ngramList = new ArrayList<String>();

        if (ngram == 1) {
            ngramList.addAll(Arrays.asList(queryTerms));
        }
        else {
            generateNgrams(ngram, queryTerms, ngramList);
        }
        return ngramList;

    }

    private static void generateNgrams (int gram, String[] qTerms, List<String> ngramList) {

        for (int k = 0; k < (qTerms.length - gram + 1); k++) {
            String s = "";
            int start = k;
            int end = k + gram;
            for (int j = start; j < end; j++) {
                s = s + qTerms[j] + SEP;
            }
            ngramList.add(s);
        }



    }

}
