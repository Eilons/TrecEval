# TrecEval
Java implementation of many well known IR evaluation measures

Getting Started

This is a maven project that was built using java version 1.8.0 Use the Maven build tool to create a single executable jar as follows. Inside the project directory type: "mvn clean install -DskipTests"

To run the project you should provide the following arguments: 

"-q" (cSeed) - qrel file. 
"-r" - Prediction file (or directory with predicition files). Files type must be ".res". 
"-o" - Out path directory
"-d" - Optional. Pass this argument if you want predicition per document

java -jar "the executable jar" -q "Path to qrel file" -r "Path to predicition file.res" -o "Out path" -d
