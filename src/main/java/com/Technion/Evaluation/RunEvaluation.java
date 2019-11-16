package com.Technion.Evaluation;

import com.Technion.Utils.ParseCmd;
import com.Technion.Utils.Utils;
import com.google.common.collect.Table;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.*;

/**
 * This class computes different evaluation metrics for a score file
 */
public class RunEvaluation {

    // Out put format
    private static final String OUT_FORMAT = "%s   %s   %s";
    // Precision format
    private static final String PrecisionAtK = "P_%d";
    private static final String PrecisionAll = "Precision";
    // Recall format
    private static final String RecallAtK = "Recall@%d";
    private static final String RecallAll = "Recall";
    // MRR format
    private static final String MRR = "MRR";
    // MAP format
    private static final String MAP = "map";
    // F1 format
    private static final String F1_FORMAT = "F1@%d";
    // ROUGE format
    private static final String ROUGE_F1 = "ROUGE_F1"+"-%d";
    // NDCG format
    private static final String NDCGAtK = "NDCG@%d";
    private static final String NDCGAll = "NDCG";


    // avg results for all
    private static final String ALL = "all";


    // qrels for all the queries for a specific article
    private Table<String, String, Integer> qrels; // rows:uuid, cols: query tokenization form, val: reljudgments
    // query -> docId -> score (query->score are ordered per insertion to the map => the res file must be sorted before)
    private Map<String,Map<String,Double>> resPerformance;
    private LinkedHashMap<String, LinkedHashMap<String,Double>> evalMeasures; // uuid -> eval Measure -> value

    public RunEvaluation (String qrelPath, File resPath) {

        this.qrels = ScoreFileReader.getLabels(new File(qrelPath));
        this.resPerformance = ScoreFileReader.getScoresPerDoc(resPath);
        this.evalMeasures = new LinkedHashMap<String, LinkedHashMap<String,Double>>();
    }

    public void getNDCG(){
        double avgNDCGAtK = 0;
        int queriesWithQrels = 0;
        //loop over queries
        for (String queryId : this.resPerformance.keySet()){

            if (this.qrels.row(queryId).isEmpty()) continue;

            queriesWithQrels++;
            //sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryNDCG = NDCG.computeNdcgAtK(rankedDocsPerQuery,this.qrels.row(queryId),rankedDocsPerQuery.size());
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(NDCGAll), queryNDCG);
            avgNDCGAtK+=queryNDCG;
        }
        avgNDCGAtK = avgNDCGAtK / (double)queriesWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(NDCGAll),avgNDCGAtK);
    }

    public void getNDCGK (int k){

        double avgNDCGAtK = 0;
        int queriesWithQrels = 0;
        //loop over queries
        for (String queryId : this.resPerformance.keySet()){

            if (this.qrels.row(queryId).isEmpty()) continue;

            queriesWithQrels++;
            //sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryNDCG = NDCG.computeNdcgAtK(rankedDocsPerQuery,this.qrels.row(queryId),k);
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(NDCGAtK,k), queryNDCG);
            avgNDCGAtK+=queryNDCG;
        }
        avgNDCGAtK = avgNDCGAtK / (double)queriesWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(NDCGAtK,k),avgNDCGAtK);
    }

    public void getPrecision () {

        double avgPrecAtK = 0;
        int queriesWithQrels = 0;
        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queriesWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                    ScoreFileReader.DESCENDING).keySet());

            double queryPrec = Precision.computePrecisionAtK(rankedDocsPerQuery, this.qrels.row(queryId), rankedDocsPerQuery.size());
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(PrecisionAll), queryPrec);
            avgPrecAtK+=queryPrec;
        }
        avgPrecAtK = avgPrecAtK / (double)queriesWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(PrecisionAll),avgPrecAtK);
    }


    public void getPrecisionK (int k) {

        double avgPrecAtK = 0;
        int queriesWithQrels = 0;
        //loop over queries
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queriesWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryPrec = Precision.computePrecisionAtK(rankedDocsPerQuery, this.qrels.row(queryId), k);
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(PrecisionAtK,k), queryPrec);
            avgPrecAtK+=queryPrec;
        }
        avgPrecAtK = avgPrecAtK / (double)queriesWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(PrecisionAtK,k),avgPrecAtK);
    }

    public void getRecall () {

        double avgRecallAtK = 0;
        int queriesWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queriesWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryRecall = Recall.computeRecallAtK(rankedDocsPerQuery, this.qrels.row(queryId), rankedDocsPerQuery.size());
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(RecallAll), queryRecall);
            avgRecallAtK+=queryRecall;
        }
        avgRecallAtK = avgRecallAtK / (double)queriesWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(RecallAll),avgRecallAtK);
    }


    public void getRecallK (int k) {

        double avgRecallAtK = 0;
        int queryWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queryWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryRecall = Recall.computeRecallAtK(rankedDocsPerQuery, this.qrels.row(queryId), k);
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(RecallAtK,k), queryRecall);
            avgRecallAtK+=queryRecall;
        }
        avgRecallAtK = avgRecallAtK / (double)queryWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(RecallAtK,k),avgRecallAtK);
    }

    /**
     * Compute MAP for a set of rankings. It is computed as
     * <br />
     * 1/|Q| sum_i^|Q|(APi)
     * <br />
     * where rank_i represents the first rank in which a relevant instance has
     * been positioned see
     */
    public void getMAP () {

        double map = 0;
        int queryWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queryWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryAP = AveP.computeAveragePrecision(rankedDocsPerQuery, this.qrels.row(queryId));
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(MAP), queryAP);
            map += queryAP;
        }
        map = map / (double)queryWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(MAP),map);
    }

    /**
     * Compute F1 for a set of rankings. It is computed as
     * <br />
     * 1/|Q| sum_i^|Q|(F1i)
     * <br />
     * where rank_i represents the first rank in which a relevant instance has
     * been positioned see
     */
    public void getF1 (int k) {

        double f1 = 0;
        int queryWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queryWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryF1 = F1Measure.computeF1Measure(rankedDocsPerQuery, this.qrels.row(queryId), k);
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(F1_FORMAT,k), queryF1);
            f1 += queryF1;
        }
        f1 = f1 / (double)queryWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(F1_FORMAT,k),f1);
    }

    /**
     * Compute MRR for a set of rankings. It is computed as
     * <br />
     * 1/|Q| sum_i^|Q|(1/rank_i)
     * <br />
     * where rank_i represents the first rank in which a relevant instance has
     * been positioned see
     */
    public void getMRR () {

        double mrr = 0;
        int queryWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queryWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double articleRR = ReciprocalRank.computeReciprocalRank(rankedDocsPerQuery, this.qrels.row(queryId));
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(MRR), articleRR);
            mrr += articleRR;
        }
        mrr = mrr / (double)queryWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(MRR),mrr);
    }

    /**
     * Compute ROUGE-N_F1 for a set of rankings. It is computed as
     * <br />
     * 1/|Q| sum_i^|Q|(APi)
     * <br />
     * where rank_i represents the first rank in which a relevant instance has
     * been positioned see
     */
    public void getROUGE (int ngram) {

        double rouge = 0;
        int queryWithQrels = 0;

        //loop over uuids
        for (String queryId : this.resPerformance.keySet()) {

            if (this.qrels.row(queryId).isEmpty()) continue;

            queryWithQrels++;
            // sort by score to ensure correct rank ordering
            List<String> rankedDocsPerQuery = new ArrayList<String>(
                    ScoreFileReader.sortMapByValues(this.resPerformance.get(queryId),
                            ScoreFileReader.DESCENDING).keySet());

            double queryROUGE = Rouge.computeRougeMaxF1(rankedDocsPerQuery, this.qrels.row(queryId),ngram);
            //update evalMeasures Table
            updateEvalMeasureTable(queryId, String.format(ROUGE_F1,ngram), queryROUGE);
            rouge += queryROUGE;
        }
        rouge = rouge / (double)queryWithQrels;
        //update evalMeasures Table
        updateEvalMeasureTable(ALL,String.format(ROUGE_F1,ngram),rouge);
    }

    /**
     * Updates the eval measure table
     * @param query
     * @param evalName
     * @param evalVal
     */
    private void updateEvalMeasureTable(String query, String evalName, double evalVal) {

        if (evalMeasures.containsKey(query)) {
            LinkedHashMap<String,Double> evalMap = evalMeasures.get(query);
            evalMap.put(evalName,evalVal);
        }
        else {
            LinkedHashMap<String,Double> evalMap = new LinkedHashMap<String,Double>();
            evalMap.put(evalName,evalVal);
            evalMeasures.put(query,evalMap);
        }
    }


    public static void main(String[] args) {

        Options options = new Options();


        Option qrelP = new Option("q","qrels",true,"qrel file");
        qrelP.setRequired(true);
        options.addOption(qrelP);

        Option resP = new Option("r","Res",true,"Prediction file (or .res directory)");
        resP.setRequired(true);
        options.addOption(resP);

        Option outP = new Option("o","OutPath",true,"Out path directory");
        outP.setRequired(true);
        options.addOption(outP);

        Option dOpt = new Option("d","PerDoc",false,"Out per document - No arguments (default is false)");
        dOpt.setRequired(false);
        options.addOption(dOpt);

        CommandLine cmd = ParseCmd.parse(options,args);
        String qrelPath = cmd.getOptionValue("qrels");
        String resPath = cmd.getOptionValue("Res");
        String outPath = cmd.getOptionValue("OutPath");
        Boolean dOutOption = cmd.hasOption("PerDoc")? true : false;

        List<File> resFiles = findResFilePaths (resPath);
        for (File resF : resFiles) {
            RunEvaluation runEvaluation = new RunEvaluation(qrelPath, resF);
            runEvaluation (runEvaluation);
            printEvalMeasures (runEvaluation.getEvalMeasures(),
                    FilenameUtils.removeExtension(resF.getName()).concat(".out"),
                    outPath, dOutOption);
        }
    }

    /**
     * Load all .res files
     * Examine if the input is a single .res file or a directory of .res files
     * @param resPath
     * @return
     */
    private static List<File> findResFilePaths(String resPath) {

        List<File> resFileList = new ArrayList<File>();
        Utils.walk(new File(resPath),resFileList);
        return resFileList;
    }

    private static void runEvaluation(RunEvaluation runEvaluation ) {

        //eval measures
        runEvaluation.getMAP();

        runEvaluation.getPrecisionK(5);
        runEvaluation.getPrecisionK(10);
        runEvaluation.getPrecisionK(20);

        runEvaluation.getNDCGK(1);
        runEvaluation.getNDCGK(3);
        runEvaluation.getNDCGK(5);
        runEvaluation.getNDCGK(10);
        runEvaluation.getNDCGK(20);


        //runEvaluation.getPrecision();


        //runEvaluation.getMRR();

        /*runEvaluation.getF1(1);
        runEvaluation.getF1(2);
        runEvaluation.getF1(5);
        runEvaluation.getF1(10);

        runEvaluation.getROUGE(1);
        runEvaluation.getROUGE(2);
*/
    }

    public LinkedHashMap<String,LinkedHashMap<String,Double>> getEvalMeasures()
    { return this.evalMeasures;}

    /**
     * Print results to file
     * @param fileName
     * @param outPath
     */
    private static void printEvalMeasures(LinkedHashMap<String,LinkedHashMap<String,Double>> evalMeasures,
                                          String fileName,
                                          String outPath, boolean dOutOption) {

        //loop over table
        if (dOutOption) {
            for (String key : evalMeasures.keySet()) {

                if (key.equals(ALL)) continue; // write at the bottom of the file
                for (Map.Entry<String, Double> entryEval : evalMeasures.get(key).entrySet()) {
                    Utils.writeString(String.format(OUT_FORMAT, entryEval.getKey(), key, entryEval.getValue()),
                            outPath.concat("/" + fileName), true);
                }
            }
        }
        // out All
        for (Map.Entry<String,Double> entryEval : evalMeasures.get(ALL).entrySet()) {
            Utils.writeString(String.format(OUT_FORMAT,entryEval.getKey(),ALL,entryEval.getValue()),
                    outPath.concat("/"+fileName), true);
        }
    }

}
