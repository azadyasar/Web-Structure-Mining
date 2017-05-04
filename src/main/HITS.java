package main;


/* Pseudo Code
    1) Start with each node having a hub score and authority score of 1.
    2) Run the Authority Update Rule
    3) Run the Hub Update Rule
    4) Normalize the values by dividing each Hub score by the sum of the squares of all Hub scores, and dividing each
    Authority score by the sum of the squares of all Authority scores.
    5)Repeat from the second step as necessary.
    Ref. https://wellrounded.wordpress.com/2010/11/13/data-mining-hits-algorithm-example-in-tcl/
 */

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class HITS extends Thread{

    /* Contains the matrix of graph which represents edges and vertexes
    *   graphOfNet.get(url) returns another hashmap containing url-integer
    *   pairs which represents url that referenced to and # of times this
    *   url is referenced
    * */
    private HashMap<String, HashMap<String, Integer>> graphOfNet_;
    /* Contains Hub Scores of websites. Initial Hubs will be determined
     * by @initialScore */
    private HashMap<String, Double> hubsMap_;
    /* Contains Authority Scores of websites. Initial Authorities will be determined
     * by @initialScore */
    private HashMap<String, Double> authMap_;
    /* At the beginning all websites' PageRank is initialized as below */
    private final double initialScore = 1.0;
    private int iterationMax_ = 50;
    private int counter;

    private MainFrame mainFrame_;


    public HITS(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet) {
        graphOfNet_ = new HashMap<>();
        hubsMap_ = new HashMap<>();
        authMap_ = new HashMap<>();
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
        runHubsAndAuths();
    }

    private void initHubsandAuthorities() {
        Set<String> keySet = graphOfNet_.keySet();
        for (String keyTmp : keySet) {
            hubsMap_.put(keyTmp, initialScore);
            authMap_.put(keyTmp, initialScore);
        }
        System.out.printf("hsize: %d, asize: %d", hubsMap_.size(), authMap_.size());
        for (String keyTmp : keySet) {
            System.out.printf("H%s - %.2f\n", keyTmp, hubsMap_.get(keyTmp));
            System.out.printf("A%s - %.2f\n", keyTmp, authMap_.get(keyTmp));
        }
    }

    private void runHubsAndAuths() {
        initHubsandAuthorities();
        for (counter = 0; counter < iterationMax_; counter++) {
            Set<String> urlSet = authMap_.keySet();
            for (String urlTmp : urlSet) {
//                System.out.printf("Calculating PR for %s\n", urlTmp);
                Set<String> subUrlSet = graphOfNet_.keySet();
                Double summation = 0.0;
                for (String subUrlTmp : subUrlSet) {
                    if (urlTmp.compareToIgnoreCase(subUrlTmp) != 0) {
//                        System.out.printf("\tFrom %s --> %s\n", subUrlTmp, urlTmp);
                        if (graphOfNet_.get(subUrlTmp).get(urlTmp) != null)
                            summation +=  hubsMap_.get(subUrlTmp);
                    }
                }
                authMap_.put(urlTmp, summation);
                System.out.printf("Putting to auth %s- %.4f\n", urlTmp, summation);
            }

            /*for (String keyTmp : urlSet) {
                System.out.printf("H%s - %.2f\n", keyTmp, hubsMap_.get(keyTmp));
                System.out.printf("A%s - %.2f\n", keyTmp, authMap_.get(keyTmp));
            }*/
            Set<String> hubsUrlSet = hubsMap_.keySet();
   /*         System.out.println(hubsUrlSet.size());
            System.out.println(hubsMap_.size() + " " + hubsMap_.keySet().size());*/
            for (String urlTmp : hubsUrlSet) {
//                System.out.printf("Calculating PR for %s\n", urlTmp);
                Set<String> subUrlSet = graphOfNet_.get(urlTmp).keySet();
                Double summation = 0.0;
                for (String subUrlTmp : subUrlSet) {
                    if (urlTmp.compareToIgnoreCase(subUrlTmp) != 0) {
//                        System.out.printf("\tFrom %s --> %s\n", subUrlTmp, urlTmp);
                        if (authMap_.get(subUrlTmp) != null)
                            summation += authMap_.get(subUrlTmp);

                    }
                }
                hubsMap_.put(urlTmp, summation);
                System.out.printf("Putting to hubs %s- %.4f\n", urlTmp, summation);
            }

            normalizeHubsandAuths();
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

        try {
            EventQueue.invokeAndWait(new Runnable() {
                @Override
                public void run() {
                    Map<String, Double> descendingPageRanks = new LinkedHashMap<>();
                    descendingPageRanks = MapUtil.sortByValue(authMap_);
                    Set<String> keySet = descendingPageRanks.keySet();
                    StringBuilder stringBuilder = new StringBuilder("<html><table>");
                    stringBuilder.append("<tr><td align='right'>ID</td><td align='center'>URL</td><td align='left'>" +
                        "Hubs / Authorities</td></tr>");
                    int counter = 1;
                    for (String keyTmp : keySet)
                        stringBuilder.append("<tr><td align='right'>").append(counter++).append("</td><td>")
                            .append(keyTmp).append("</td><td align='left'>").append(String.format("%.5f", hubsMap_.get(keyTmp)))
                            .append(" / ").append(String.format("%.5f",descendingPageRanks.get(keyTmp))).append("</td></tr>");
                    mainFrame_.getFoundUrlsTextPane().setText(stringBuilder.toString());
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void normalizeHubsandAuths() {
        double totalHubScore = 0.0;
        double totalAuthScore = 0.0;
        Set<String> urlSet = hubsMap_.keySet();
        for (String urlTmp : urlSet) {
            double tmp = authMap_.get(urlTmp);
            totalAuthScore += tmp * tmp;
            tmp = hubsMap_.get(urlTmp);
            totalHubScore += tmp * tmp;
        }

        totalAuthScore = Math.sqrt(totalAuthScore);
        totalHubScore = Math.sqrt(totalHubScore);

        for (String urlTmp : urlSet) {
            double tmp = authMap_.get(urlTmp) / totalAuthScore;
            authMap_.put(urlTmp, tmp);
            tmp = hubsMap_.get(urlTmp) / totalHubScore;
            hubsMap_.put(urlTmp, tmp);
        }
//        printHubsandAuth();
    }

    private void printHubsandAuth() {
        Set<String> keySet = graphOfNet_.keySet();
        for (String keyTmp : keySet) {
            System.out.printf("H%s - %.4f\n", keyTmp, hubsMap_.get(keyTmp));
            System.out.printf("A%s - %.4f\n", keyTmp, authMap_.get(keyTmp));
        }
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame_ = mainFrame;
    }

    public HashMap<String, HashMap<String, Integer>> getGraphOfNet() {
        return this.graphOfNet_;
    }

    public HashMap<String, Double> getAuthMap() {
        return this.authMap_;
    }

}
























