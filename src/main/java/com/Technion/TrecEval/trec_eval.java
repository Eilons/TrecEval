package com.Technion.TrecEval;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

import com.Technion.Utils.Utils;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class trec_eval {

	public static void main(String[] args) throws IOException {
		
		if (args.length < 2) {
			System.out.println("need to pass three arguments: 1. qrel file 2. res directory 3. output directory");
			System.out.println("for ablation test, need two arguments: 1. qrel file 2. No directories used as input and out directories");
			System.exit(1);
		}
		System.out.println("Evaluation Files");
		Table<String,String,Integer> qrelTable = setQrelTable (args[0]);
		if (args.length == 3) {
			List<File> resPathList = getResultsFilesFromDirectory(args[1]);
			for (File file : resPathList) {
				
				String outPutFileName = FilenameUtils.removeExtension(file.getName());
				System.out.println(outPutFileName);
				outPutFileName = args[2].concat("/"+outPutFileName+".out");
				// evaluate runs
				EvalRun(file, qrelTable,outPutFileName);
			}
	
		}
		
	if (args.length == 2) {
		File[] directories = new File(args[1]).listFiles();//get all directories from path
		for (File directory : directories) {
			File resFile = findResFile (directory);
			String outPutFileName = FilenameUtils.removeExtension(resFile.getName());
			System.out.println(outPutFileName);
			outPutFileName = directory.getAbsolutePath().concat("/"+outPutFileName.toString()+".out");
			EvalRun(resFile, qrelTable,outPutFileName);
		}
	}
	System.out.println("DONE!");
}

	/**
	 * qrels in structure 451-> WTX081-B43-383 -> 1
     * 						 -> WTX097-B19-147 -> 1
	 * @param qrelPath
	 * @return
	 */
	private static Table<String,String,Integer> setQrelTable(String qrelPath) {

	    Table<String,String,Integer> qrelTable = HashBasedTable.create();
        try {
            List<String> qrelList = FileUtils.readLines(new File (qrelPath));
            for (String qrelRow : qrelList) {
                String[] qrelArray = qrelRow.split("\\s+");
                qrelTable.put(qrelArray[0],qrelArray[2],Integer.parseInt(qrelArray[3]));
            }


        } catch (IOException e) {
            System.out.println("Error in reading qrelFile");
            e.printStackTrace();
            System.exit(1);
        }
        return qrelTable;
    }

	/**
	 * Find .res file in directory
	 * @param directory
	 * @return
	 */
	private static File findResFile(File directory) {
		
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.getName().contains(".res")) {
				return file;
			}
		}
		
		System.out.println("No res file was found");
		System.exit(1);
		return null;
	}


	private static void EvalRun(File resFile,
			Table<String,String,Integer> qrelStructure,
			String outPutFileName) throws IOException  {
		
		//open file for output results
        try {
			BufferedWriter wr = new BufferedWriter (new OutputStreamWriter(new FileOutputStream(outPutFileName), "UTF-8"));

			//read resFile line by line
			int topicsWithQrels = 0;
			Map<String,List<String>> topicDocMap = loadRankingDocPerTopic (FileUtils.readLines(resFile,"UTF-8"));
			List<String> topicList = new ArrayList<String>(topicDocMap.keySet());
			Double ap_All = 0.0; //MAP
			Double p_5_All  = 0.0; // holds Average p_5
			Double p_10_All  = 0.0; // holds Average p_10
			Double p_20_All  = 0.0; // holds Average p_20
			String content = "";
			for (String topic : topicList) {
				if (!qrelStructure.containsRow(topic)) {
					continue;
				}
				topicsWithQrels ++; // Only if the topic has qrels than add to the count
				double p_5 = calcPrecision(topicDocMap.get(topic),qrelStructure.row(topic),5);
				double p_10 = calcPrecision(topicDocMap.get(topic),qrelStructure.row(topic),10);
				double p_20 = calcPrecision(topicDocMap.get(topic),qrelStructure.row(topic),20);
				double map = calcAP(topicDocMap.get(topic),qrelStructure.row(topic));
				
				 content += "map     " + topic + " " + map +"\n";
				 content += "P_5     " + topic + " " + p_5 +"\n";
				 content += "P_10    " + topic + " " + p_10 +"\n";
				 content += "P_20    " + topic + " " + p_20 +"\n";
				
				ap_All += map ;
				p_5_All += p_5 ;
				p_10_All += p_10;
				p_20_All += p_20;
				
			}
			//Avg
			ap_All = ap_All / topicsWithQrels;
			p_5_All = p_5_All / topicsWithQrels ;
			p_10_All = p_10_All / topicsWithQrels ;
			p_20_All = p_20_All / topicsWithQrels ;
			
			 content += "map     " + "all" + " " + ap_All +"\n";
			 content += "P_5     " + "all" + " " + p_5_All +"\n";
			 content += "P_10    " + "all" + " " + p_10_All +"\n";
			 content += "P_20    " + "all" + " " + p_20_All +"\n";
        
			 System.out.println("Print evalution file");
	            wr.write(content);
	            wr.flush();
	            wr.close();
	            
        
        } catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        
		
	}

	/**
	 * 
	 * @param returnedDocumentIdList
	 * @param relDocumentQrel
	 * @return
	 */
	private static double calcAP(List<String> returnedDocumentIdList,
			Map<String,Integer> relDocumentQrel) {
		
		int countRel = getRelDocumentCount (relDocumentQrel);
		double ap =0;
		List<Integer> relPositions = getRelPer (returnedDocumentIdList,relDocumentQrel);
		for (int i=0 ; i< relPositions.size(); i++) {
			ap += (i+1)/(double)relPositions.get(i);
		}
		if (countRel == 0) {
			return ap;
		}
		else{
			return ap*(1/(double)countRel);
		}
	}

	
	

/**
 * 
 * @param returnedDocumentIdList
 * @param relDocumentQrel
 * @return relevant positions in res file of relevant documents
 */
private static List<Integer> getRelPer(List<String> returnedDocumentIdList,
			Map<String,Integer> relDocumentQrel) {
		
	List<Integer> relPositionList = new ArrayList<Integer>();
	for (int i=0; i< returnedDocumentIdList.size(); i++){

	    if (relDocumentQrel.containsKey(returnedDocumentIdList.get(i))){
	        if (relDocumentQrel.get(returnedDocumentIdList.get(i)) > 0){
                relPositionList.add(i+1);
            }
        }
    }
	return relPositionList;
}


/**
 * 
 * @param relDocumentQrel
 * @return the number of relevant document in qrel file for some topic
 */
	private static int getRelDocumentCount(Map<String,Integer> relDocumentQrel) {
		
		int count =0;
		for (Map.Entry<String, Integer> entry : relDocumentQrel.entrySet()){
			if (entry.getValue() > 0)
				count++;
		}
		return count;
	}


	/**
	 *
	 * @param returnedDocumentIdList
	 * @param preLevel - precision level
	 * @return
	 */
	private static double calcPrecision(List<String> returnedDocumentIdList,
                                        Map<String,Integer> qrel, int preLevel) {

		Utils.ifFalseCrash(!qrel.isEmpty(), "Qrel must be non empty - Precision");
		Utils.ifFalseCrash(preLevel > 0, "preLevel should be > 0");

		double precision = 0.0;
		int tp=0;
		int i;
		for (i=0; i<Math.min(returnedDocumentIdList.size(), preLevel); i++) {
			if (qrel.containsKey(returnedDocumentIdList.get(i)) ) {
				if (qrel.get(returnedDocumentIdList.get(i)) > 0){
					tp++;         //if true, the query is relevant
				}
			}
		}
		precision = (double)tp / (double)preLevel;
		return precision;
	}


	/**
	 * extract all topics from res file
	 * @param resFileSplitedList
	 * @return
	 */
	private static List<String> getTopicsFromResFile(
			List<String[]> resFileSplitedList) {
		
		List<String> topicList = new ArrayList<String> ();
		Set<String> topicSet = new HashSet<String>();
		for (String[] line : resFileSplitedList) {
			
			if (!topicSet.contains(line[0])) {
				topicSet.add(line[0]);
				topicList.add(line[0]);
			}
		}
		return topicList;
	}


	public static List<File> getResultsFilesFromDirectory(String pathDir) throws FileNotFoundException
	{
		List<File> FilesQueriesDirectories = new ArrayList<File>();
		FilesQueriesDirectories =  getAllFilesNames(pathDir);
		
		return FilesQueriesDirectories;		
	}
    
	private static List<File> getAllFilesNames(String rootDirectory) throws FileNotFoundException {
		File root = new File(rootDirectory);
		checkIfDirectoryExists(rootDirectory, root);
		File[] listFiles = root.listFiles();
		return new ArrayList<File>(Arrays.asList(listFiles));
	}
	
	private static void checkIfDirectoryExists(String rootDirectory, File root)
			throws FileNotFoundException {
		if (!root.isDirectory()) {
			throw new FileNotFoundException(rootDirectory + " isn't  drectory");
		}
	}
	
	/**
	 * get List of String files
	 * @param readLines
	 * @return each line is now array of separated words
	 */
	public static List<String[]> getSplitedFile(List<String> readLines) {
		
		List<String[]> splitedList = new ArrayList<String[]>();
		for (String line : readLines) {
			String[]  lineArray = splitToTokens(line);
			splitedList.add(lineArray);
		}
		
		return splitedList;
	}
	
	// for each line return array of strings containing only tokens (no spaces)
	public static String[] splitToTokens (String line)
	{
			String[] strings = line.split("\\s+");
			return strings;
	}

	private static Map<String,List<String>> loadRankingDocPerTopic(List<String> fileLines) {

		Map<String,List<String>> topicRankingPath = new LinkedHashMap<String,List<String>>();
		for (String line : fileLines) {
			String[] colls = Utils.splitBySpace(line);
			if (topicRankingPath.containsKey(colls[0])){
				List<String> docRanking = topicRankingPath.get(colls[0]);
				docRanking.add(colls[2]);
			}
			else {
				List<String> docRanking = new ArrayList<String>();
				docRanking.add(colls[2]);
				topicRankingPath.put(colls[0],docRanking);
			}
		}
		return topicRankingPath;
	}

}
