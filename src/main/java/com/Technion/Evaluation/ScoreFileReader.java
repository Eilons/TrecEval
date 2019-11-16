package com.Technion.Evaluation;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ScoreFileReader {
    /**Use if the order of the elements should be ascending  */
    public static final boolean ASCENDING = true;

    /**Use if the order of the elements should be descending. This is the right one for rankings!  */
    public static final boolean DESCENDING = false;

    private static final char FIELD_SEPARATOR = '\t';

    /** The official format for the output file contains four fields */
    private static final int EXPECTED_QRELS_FIELDS = 4;
    private static final int EXPECTED_RES_FIELDS = 6;


    /** Location of the query id. */
    private static final int LOC_QueryId = 0;
    /** Location of the docId id. */
    private static final int LOC_DocId = 2;

    /** Location of the rank of the document with respect to the query */
    private static final int LOC_RANK = 3;
    /** Location of the score of the document with respect to the query */
    private static final int LOC_SCORE = 4;

    /** Location of the label of the document with respect to the query*/
    private static final int LOC_LABEL = 3;

    /**
     * load qrel file to table
     * @param qrelFile path to qrel file
     * @return Table with the following structure: query -> docId -> relJudg
     */
    public static Table<String,String,Integer> getLabels(File qrelFile) {
        Table<String,String,Integer> tableData = HashBasedTable.create();
        List<ScoreRecord> records = loadDataset(qrelFile, EXPECTED_QRELS_FIELDS);
        for (ScoreRecord sr : records) {
            tableData.put(sr.query,sr.docId,sr.relJudg);
        }
        return tableData;
    }


    /**
     * Get the scores assigned to each instance in the score file
     * @param scoreFile path to the file with scores and labels
     * @return map with docId IDs and internal maps with query IDs and scores
     */
    public static Map<String, Map<String, Double>> getScoresPerDoc(File scoreFile) {
        Map<String, Map<String, Double>> scoreMap = new LinkedHashMap<String, Map<String, Double>>();
        List<ScoreRecord> records = loadDataset(scoreFile, EXPECTED_RES_FIELDS);
        for (ScoreRecord sr : records) {
            if (! scoreMap.containsKey(sr.query)) {
                scoreMap.put(sr.query, new LinkedHashMap<String, Double>());
            }
            scoreMap.get(sr.query).put(sr.docId, sr.score);
        }
        return scoreMap;
    }



    /**
     * Loads the input file in List<ScoreRecord>
     * @param scoreFile input score file
     * @return List of ScoreRecords, with all the fields in the file.
     */
    private static List<ScoreRecord> loadDataset(File scoreFile, int expectedCollumns) {

        List<ScoreRecord> records = new ArrayList<ScoreRecord>();
            List<String> fileRows = Utils.readLines(scoreFile);
            for (String row : fileRows) {
                String[] splitedRow = Utils.splitBySpace(row);
                Utils.ifFalseCrash(splitedRow.length == expectedCollumns,
                        String.format("Line %s in file %s has %d fields: %d expected",
                                row, scoreFile, splitedRow.length, expectedCollumns));

                ScoreRecord sr = new ScoreRecord();
                sr.query = splitedRow[LOC_QueryId];
                sr.docId = splitedRow[LOC_DocId];
                sr.relJudg = Integer.parseInt(splitedRow[LOC_LABEL]);
                // More than 3 columns its a res file
                if (splitedRow.length > 4) {
                    sr.rank = Integer.parseInt(splitedRow[LOC_RANK]);
                    sr.score = Double.parseDouble(splitedRow[LOC_SCORE]);
                }
                records.add(sr);
            }
        return records;
    }

    /**
     * Sorts a map on the basis of its values.
     * @param unsortedMap
     * @param order true if ascending; false if descending
     * @return LinkedHashMap sorted by value.
     */
    public static Map<String, Double> sortMapByValues(Map<String, Double> unsortedMap,
                                                final boolean order) {
        List<Map.Entry<String, Double>> list =
                new LinkedList<Map.Entry<String, Double>>(unsortedMap.entrySet());

        // Sorting based on values
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2) {
                if (order) {
                    return o1.getValue().compareTo(o2.getValue());
                } else {
                    return o2.getValue().compareTo(o1.getValue());
                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
}


/**
 * An internal class to store all the fields of each record in the
 * ranking and qrel file.
 *
 */
class ScoreRecord {
    String query;
    String docId;
    int rank;
    double score;
    int relJudg;
}