# Web-Structure-Mining

This project demonstrates the well-known <a href="http://www.cs.princeton.edu/~chazelle/courses/BIB/pagerank.htm">PageRank</a>
and <a href="http://www.math.cornell.edu/~mec/Winter2009/RalucaRemus/Lecture4/lecture4.html">HITS</a> algorithms.

## Dependencies 

The project depends on <a href="https://jsoup.org">JSoup</a> and <a href="http://graphstream-project.org">GraphStream</a>
jars.

## Mechanism

Bare-bones version of Breadth-First Search Crawler is implemented for crawling the web. 
### You can 
    assign seed urls to crawl from.
    specify a .txt file containing keywords to filter which URLs to put in Frontier.
    specify number of threads that the crawlers will be assigned and number of iterations for crawling.
The graph of (incoming, outgoing links) the Web is stored as HashMap<WebURL, HashMap<WebUrls, Integer>> internally (in a concurrent fashion.). PageRank and HITS algorihtms can be applied on this graphs. Graphs can be exported and imported later.
You can visualize the tiny part of the Web that crawlers found out by specifying the number of nodes to be drawn based on their ranks. 

## GUI
### Main Screen
<img src="https://github.com/azadyasar/Web-Structure-Mining/blob/master/res/main_1.png" alt="https://github.com/azadyasar/Web-Structure-Mining/blob/master/res/main_1.png">

### Graph

<img src="https://github.com/azadyasar/Web-Structure-Mining/blob/master/res/graph_1.png" alt="https://github.com/azadyasar/Web-Structure-Mining/blob/master/res/graph_1.png">

    
