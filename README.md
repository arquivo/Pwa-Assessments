#PWA Assessments

Code available in maven project [PWA assessments](https://github.com/arquivo/Pwa-Assessments). Project to evaluate the relevance of the results in the archive.
The objective of the project is to create a platform to evaluate the ranking of [arquivo.pt](http://arquivo.pt), for this a web page, assessments, was created where the user evaluates the returned document for a given search. In order to assist the process, two web pages were created, consensus and review, the first to change some evaluation already done, the second to review the evaluations made.

##Web Pages

* `Assessments`: interface responsible for user evaluation, needs authentication.
* `Consensus`: view used to update the relevance value given in evaluations. From a scale of -1 to 2, where 0 is Do not know and 2 is Very Relevant, needs authentication.
* `Review`: lists document evaluations per survey, requires authentication.
* `Login`: Access is restricted by the Apache Tomcat server, a configuration rule with its rule is created for each user.

##Database

![Diagram ER database](https://github.com/arquivo/Pwa-Assessments/blob/master/diagramER-PWAassessments.png)

##Importing new documents/queries

To import new querys/documents for evaluation there is the class _QueryResultsScrapper_.

As it is a maven project we can execute the class with the mvn command, input parameters:

`<queries file> <queriesType (0-nav, 1-inf)> <ranking features (e.g. 0+1+2)> <features boosts (e.g. 1+1+1)> <database> <username> <password>`

Where:
* `queries file`: XML that defines the queries to be evaluated, [here](https://github.com/arquivo/Pwa-Assessments/blob/master/queries/topics.xml) is an example.
* `queriesType`: 0 for navigational queries and 1 for informational queries.
* `ranking features`: Ranking functions used in the search, numbered based on the list that is found on the [searchTests](http://arquivo.pt/searchTests.jsp) page.
* `features boosts`: Boosts associated with each function chosen in the above parameter.
* `database`: Database url.
* `username`: Database username.
* `password`: Database password.

Example of invocation:

`mvn exec:java -Dexec.mainClass="pt.arquivo.assessments.QueryResultsScrapper"  -Dexec.args="queries/newTopicsJN.xml 0 0+1+2 1+1+1 //127.0.0.1:5432/nutchwax nutchwax *****"`

This class will perform each search found in the input file by going to Search Tests, described in this document, performs the scrapping technique to extract the information from the page, saving the documents obtained from the search in the table docs relating to the query table where the search is saved.

##Other features
* `AutomaticAssessments`: Create automatic assessments based on existing assessments for navigational queries
* `DatasetTemporalBinaryFeatures`: Add binary features (relevant or not-relevant)
* `DataTemporalMultiplier`: Multiply features of datasets according the timestamp of a version
* `DatasetTemporalSplitter`:  Splits feature datasets by time
* `DuplicatesPerQuery`: Removes duplicates from runs and qrels files
* `FeaturesGenerator`:  Adds ranking features for all assessments
* `FleissKappa`: Generated matrix to compute Fleiss Kappa
* `MergeRuns`: Merge runs and rerank entries
* `Normalize`: Normalize feature datasets
* `Qrels`: Encapsulates qrels of a query
* `QrelsGenerator`: Creates a qrels files
* `MigrateCodeDocs`: Update the codes and urlarchived fields of the docs table that were with the old ids used in the file
 * `code`: timestamp+url

