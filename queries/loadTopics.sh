#!/bin/bash
#
#LOAD DATABASE FOR ASSESSMENTS:


#Nutch
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 34 1 //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >34features.out 2>stderr21 

#Lucene
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 32 1 //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >32features.out 2>stderr22 

#BM25
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 31 1 //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >31features.out 2>stderr23 

#Nutch + \# versions: 3 confs
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+64 0.9+0.1   //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3564-0901features.out 2>stderr24 
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+64 0.75+0.25 //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3564-075025features.out 2>stderr25 
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+64 0.5+0.5   //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3564-0505features.out 2>stderr26 

#Nutch + span versions: 3 confs
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+62 0.9+0.1   //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3562-0901features.out 2>stderr27 
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+62 0.75+0.25 //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3562-075025features.out 2>stderr28 
java -classpath /home/nutchwax/workspace/pwa-technologies/PwaArchive-access/projects/nutchwax/nutchwax-core/target/nutchwax-core-0.11.0-SNAPSHOT.jar:/home/nutchwax/workspace/pwa-technologies/PwaLucene/target/pwalucene-1.0.0-SNAPSHOT.jar:/home/nutchwax/.m2/repository/org/jsoup/jsoup/1.6.1/jsoup-1.6.1.jar:/home/nutchwax/.m2/repository/postgresql/postgresql/8.3-604.jdbc4/postgresql-8.3-604.jdbc4.jar:/home/nutchwax/workspace/PwaAssessments/target/classes/ pt.arquivo.assessments.QueryResultsScrapper /home/nutchwax/workspace/PwaAssessments/queries/topics.xml 0 35+62 0.5+0.5   //t3.tomba.fccn.pt/nutchwax nutchwax nutchx! >3562-0505features.out 2>stderr29 
