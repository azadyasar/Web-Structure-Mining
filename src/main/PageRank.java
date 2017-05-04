package main;


import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PageRank extends Thread{

    /* Contains the matrix of graph which represents edges and vertexes
    *   graphOfNet.get(url) returns another hashmap containing url-integer
    *   pairs which represents url that referenced to and # of times this
    *   url is referenced
    * */
    private HashMap<String, HashMap<String, Integer>> graphOfNet_;
    /* Contains PageRank of websites. Initial PageRanks will be determined
     * by @initalPageRank_ */
    private HashMap<String, Double> pagerankMap_;
    /* Contains the number of links that are going out from the given URL */
    private HashMap<String, Integer> outgoingLinks_;
    /* At the beginning all websites' PageRank is initialized as below */
    private final double initialPageRank_ = 1.0;
    /* Damping factor(d) which will be used as: PR(A) = (1 - d) + d * (PR(B) + ... ) */
    private final double dampingFactor_ = 0.75;
    /* How many times the iterative PageRank algorihtm will run */
    private int iterationMax_ = 50;
    private int counter;

    private MainFrame mainFrame_;

    public PageRank() {
        graphOfNet_ = new HashMap<>();
        pagerankMap_ = new HashMap<>();
        outgoingLinks_ = new HashMap<>();
        iterationMax_ = 1000;
    }

    public PageRank(HashMap<String, HashMap<String, Integer>> graphOfNet) {
        this.graphOfNet_ = graphOfNet;
        this.pagerankMap_ = new HashMap<>();
        this.outgoingLinks_ = new HashMap<>();
        iterationMax_ = 1000;
    }

    public PageRank(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet) {
        graphOfNet_ = new HashMap<>();
        pagerankMap_ = new HashMap<>();
        outgoingLinks_ = new HashMap<>();
        Set<String> keySet = graphOfNet.keySet();
        for (String keyTmp : keySet) {
            graphOfNet_.put(keyTmp, new HashMap<>());
//            System.out.printf("Putting main url: %s\n", keyTmp);
            Set<String> subKeySet = graphOfNet.get(keyTmp).keySet();
            for (String subKeyTmp : subKeySet) {
//                System.out.printf("\tAdding flow: %s - %d\n", subKeyTmp, graphOfNet.get(keyTmp).get(subKeyTmp));
                graphOfNet_.get(keyTmp).put(subKeyTmp, graphOfNet.get(keyTmp).get(subKeyTmp));
            }
        }
    }

    @Override
    public void run() {
        calculatePageRanks();
    }

    private void initPageRanks() {
        Set<String> urlSet = graphOfNet_.keySet();
        if (pagerankMap_ == null)
            pagerankMap_ = new HashMap<>();
        for (String urlTmp : urlSet) {
            pagerankMap_.put(urlTmp, initialPageRank_);
        }
    }

    private void initOutgoingLinks() {
        Set<String> urlSet = graphOfNet_.keySet();
        for (String urlTmp : urlSet) {
            int total = 0;
            Set<String> referencedLinks = graphOfNet_.get(urlTmp).keySet();
            for (String subUrlTmp : referencedLinks)
                total += graphOfNet_.get(urlTmp).get(subUrlTmp);
            outgoingLinks_.put(urlTmp, total);
        }
    }

    public void calculatePageRanks() {
        initPageRanks();
        initOutgoingLinks();
        /* Handle here */
        /*String pathToLog = "";//CrawlerThreadService.path_to_log + "PR_log_" + (new Date().getTime()) + ".txt";
        File logFile = new File(pathToLog);
        if (logFile.exists())
            logFile.delete();
        StringBuilder logString = new StringBuilder();*/
        for (counter = 0; counter < iterationMax_; counter++) {
            Set<String> urlSet = pagerankMap_.keySet();
            for (String urlTmp : urlSet) {
//                System.out.printf("Calculating PR for %s\n", urlTmp);
                Double result = 1 - dampingFactor_;
                Set<String> subUrlSet = graphOfNet_.keySet();
                Double summation = 0.0;
                for (String subUrlTmp : subUrlSet) {
                    if (urlTmp.compareToIgnoreCase(subUrlTmp) != 0) {
//                        System.out.printf("\tFrom %s --> %s\n", subUrlTmp, urlTmp);
                        if (graphOfNet_.get(subUrlTmp).get(urlTmp) != null)
                            summation +=  pagerankMap_.get(subUrlTmp) / outgoingLinks_.get(subUrlTmp);
                    }
                }
                result += dampingFactor_ * summation;
                pagerankMap_.put(urlTmp, result);
//                System.out.printf("Iter #%d\n", counter+1);
//                logString.append(String.format("Iter #%d\n", counter+1));
//                System.out.printf("PR(%s) = %.16f\n", urlTmp, pagerankMap_.get(urlTmp));
//                logString.append(String.format("PR(%s) = %.16f\n", urlTmp, pagerankMap_.get(urlTmp)));
            }
            try {
                EventQueue.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        double ratio = ((double)(1+counter) / iterationMax_);
                        System.out.println(ratio);
                        int progress = (int) new Double(100 * ratio).longValue();

                        mainFrame_.getProgressBar().setValue(progress);
                        mainFrame_.getProgressBar().setStringPainted(true);
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
//        FileReaderEx.writeToFile(logFile, logString.toString());
        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Map<String, Double> descendingPageRanks = new LinkedHashMap<>();
                    descendingPageRanks = MapUtil.sortByValue(pagerankMap_);
                    Set<String> keySet = descendingPageRanks.keySet();
                    StringBuilder stringBuilder = new StringBuilder("<html><table>");
                    stringBuilder.append("<tr><td align='right'>ID</td><td align='center'>URL</td><td align='left'>" +
                        "PageRank</td></tr>");
                    int counter = 1;
                    for (String keyTmp : keySet)
                        stringBuilder.append("<tr><td align='right'>").append(counter++).append("</td><td>")
                            .append(keyTmp).append("</td><td align='left'>").append(String.format("%.5f",
                            descendingPageRanks.get(keyTmp))).append("</td></tr>");
                    mainFrame_.getFoundUrlsTextPane().setText(stringBuilder.toString());
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    public void printPageRanks() {
        Map<String, Double> descendingPageRanks = new LinkedHashMap<>();
        descendingPageRanks = MapUtil.sortByValue(pagerankMap_);
        Set<String> urlSet = descendingPageRanks.keySet();
        StringBuilder logString = new StringBuilder();
        System.out.printf("\n###PageRanks###\n");
        logString.append(String.format("\n###PageRanks###\n"));
        int counter = 1;
        for (String urlTmp : urlSet) {
            System.out.printf("%d-PR(%s): %.16f\n", counter++,urlTmp, descendingPageRanks.get(urlTmp));
            logString.append(String.format("PR(%s): %.16f\n", urlTmp, descendingPageRanks.get(urlTmp)));
        }
       /* String pathToLog = CrawlerThreadService.PATH + "PR_log_" + (new Date().getTime()) + ".txt";
        File logFile = new File(pathToLog);
        FileReaderEx.writeToFile(logFile, logString.toString());*/

    }



    public void printGraphFlow() {
        System.out.printf("Flow of the links(%d):\n\n", graphOfNet_.size());
        Set<String> keySet = graphOfNet_.keySet();
        for (String mainUrl : keySet) {
            System.out.printf("Links from <%s>\n", mainUrl);
            Set<String> subKeySet = graphOfNet_.get(mainUrl).keySet();
            for (String subUrl : subKeySet) {
                System.out.printf("\t\t%s - %d\n", subUrl, graphOfNet_.get(mainUrl).get(subUrl));
            }
        }
    }


    /* Getters and Setters  */

    public HashMap<String, HashMap<String, Integer>> getGraphOfNet_() {
        return graphOfNet_;
    }

    public void setGraphOfNet_(HashMap<String, HashMap<String, Integer>> graphOfNet_) {
        this.graphOfNet_ = graphOfNet_;
    }

    public HashMap<String, Double> getPagerankMap_() {
        return pagerankMap_;
    }

    public void setPagerankMap_(HashMap<String, Double> pagerankMap_) {
        this.pagerankMap_ = pagerankMap_;
    }

    public double getInitialPageRank_() {
        return initialPageRank_;
    }

    public double getDampingFactor_() {
        return dampingFactor_;
    }

    public int getIterationMax_() {
        return iterationMax_;
    }

    public void setIterationMax_(int iterationMax_) {
        this.iterationMax_ = iterationMax_;
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame_ = mainFrame;
    }
}
