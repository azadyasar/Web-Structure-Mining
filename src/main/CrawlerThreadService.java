package main;


import org.jsoup.nodes.Document;
import sun.rmi.runtime.Log;

import java.awt.*;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CrawlerThreadService extends Thread{

    private ConcurrentHashMap<String, Integer> crawledLinks_;
    private ConcurrentLinkedQueue<String> frontierQueue_;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet_;
    private int ITERATION_MAX;
    private int THREAD_SIZE;
    private String path_to_log = "./log/";
    private MainFrame mainFrame_;
    private String seedUrl_;
    private HashMap<String, Integer> filterKeywords_;
    private boolean stopOrder;
    private Logger logger_;

    private int counter_iter;

    public CrawlerThreadService() {
        crawledLinks_ = new ConcurrentHashMap<>();
        frontierQueue_ = new ConcurrentLinkedQueue<>();
        graphOfNet_ = new ConcurrentHashMap<>();
        ITERATION_MAX = 300;
        THREAD_SIZE = 30;
        filterKeywords_ = new HashMap<>();
        stopOrder = false;
        File logFolder = new File(path_to_log);
        if (!logFolder.exists())
            logFolder.mkdir();
        Logger.setFolder(logFolder);
        this.logger_ = Logger.getLogger();
    }

    public CrawlerThreadService(int max_iteration, int thread_size, String seedUrl) {
        crawledLinks_ = new ConcurrentHashMap<>();
        frontierQueue_ = new ConcurrentLinkedQueue<>();
        graphOfNet_ = new ConcurrentHashMap<>();
        this.ITERATION_MAX = max_iteration;
        this.THREAD_SIZE = thread_size;
        this.seedUrl_ = seedUrl;
        filterKeywords_ = new HashMap<>();
        stopOrder = false;
        File logFolder = new File(path_to_log);
        if (!logFolder.exists())
            logFolder.mkdir();
        Logger.setFolder(logFolder);
        this.logger_ = Logger.getLogger();
    }

    @Override
    public void run() {
        executeCrawlers();
    }


    public void executeCrawlers() throws NumberFormatException{



        Document doc;
        if (isLinkOk(seedUrl_)) {
            if (!seedUrl_.endsWith("/"))
                seedUrl_ = seedUrl_ + "/";
            seedUrl_ = seedUrl_.replace(" ", "");
            if (seedUrl_.startsWith("https"))
                seedUrl_ = "http://" + seedUrl_.substring(8);
            if (!seedUrl_.contains("www")) {
                if (seedUrl_.startsWith("https"))
                    seedUrl_ = "http://www." + seedUrl_.substring(8);
                else if (seedUrl_.startsWith("http"))
                    seedUrl_ = seedUrl_.substring(0, 7) + "www." + seedUrl_.substring(7);
            }
            System.out.println("\n" + seedUrl_);
            frontierQueue_.add(seedUrl_);
        } else {
            System.err.printf("Provide a proper seedUrl_.\n");
            logger_.log("Provide a proper seedURL.\n");
            throw new NumberFormatException();
        }

        counter_iter = 0;
        while (counter_iter++ < ITERATION_MAX) {
            if (!stopOrder) {
                StringBuilder logString = new StringBuilder();
                Crawler crawlers[] = new Crawler[THREAD_SIZE];
                for (Crawler crawler : crawlers)
                    crawler = null;
                if (frontierQueue_.size() > THREAD_SIZE + 1) {
                    for (int i = 0; i < THREAD_SIZE; i++) {
                        String nextUrl = frontierQueue_.remove();
//                    System.out.printf("At <%s>\n", nextUrl);
                        logString.append(String.format("At <%s>\n", nextUrl));
                        crawlers[i] = new Crawler(nextUrl, frontierQueue_, graphOfNet_, crawledLinks_);
                        crawlers[i].setFilterKeywords(filterKeywords_);
                        crawlers[i].start();
                    }
                }

                if (frontierQueue_.size() > 1 && THREAD_SIZE > 1) {
                    String nextUrl = frontierQueue_.remove();
//                System.out.printf("At <%s>\n", nextUrl);
                    logString.append(String.format("At <%s>\n", nextUrl));
                    crawlers[0] = new Crawler(nextUrl, frontierQueue_, graphOfNet_, crawledLinks_);
                    crawlers[0].setFilterKeywords(filterKeywords_);
                    crawlers[0].start();
                    nextUrl = frontierQueue_.remove();
//                System.out.printf("At <%s>\n", nextUrl);
                    logString.append(String.format("At <%s>\n", nextUrl));
                    crawlers[1] = new Crawler(nextUrl, frontierQueue_, graphOfNet_, crawledLinks_);
                    crawlers[1].setFilterKeywords(filterKeywords_);
                    crawlers[1].start();
                } else if (frontierQueue_.size() == 1) {
                    String nextUrl = frontierQueue_.remove();
//                System.out.printf("At <%s>\n", nextUrl);
                    logString.append(String.format("At <%s>\n", nextUrl));
                    crawlers[0] = new Crawler(nextUrl, frontierQueue_, graphOfNet_, crawledLinks_);
                    crawlers[0].setFilterKeywords(filterKeywords_);
                    crawlers[0].start();
                }

                for (Crawler crawler : crawlers)
                    if (crawler != null)
                        while (crawler.isAlive()) ;



            /*System.out.printf("Flow size: %d\nIter: %d\nFrontier size: %d\nCrawled so far: %d\n", graphOfNet_.size(),
                counter_iter, frontierQueue_.size(), crawledLinks_.size()); */

            logString.append(String.format("*\n*\n*\nInfo:\nFlow size: %d\nIter: %d\nFrontier size: %d\nCrawled so far: %d\n\n",
                graphOfNet_.size(), counter_iter, frontierQueue_.size(), crawledLinks_.size()));
            logger_.log(logString.toString());


            /* Update crawledUniqueUrlTextArea to show newly found URL's */
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder("<html><table>");
                            Set<String> keySet = graphOfNet_.keySet();
                            stringBuilder.append("<tr><th align ='center'>ID</th><th align='center'>URL</th></tr>");
                            int counter = 1;
                            for (String keyTmp : keySet)
                                stringBuilder.append("<tr><td align='right'>").append(counter++).append("</td><td>")
                                    .append(keyTmp).append("</td></tr>");
                            mainFrame_.getFoundUrlsTextPane().setText(stringBuilder.toString());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder("<html><table>");
                            int crawledLinksSize = crawledLinks_.size();
                            int uniqueUrlSize = graphOfNet_.size();
                            int frontierSize = frontierQueue_.size();
                            stringBuilder.append("<tr><th align ='center'><font color='red'>Statistics</font></th></tr>");
                            stringBuilder.append("<tr><td align='left'>").append("Crawled so far: </td><td align='center'>")
                                .append(crawledLinksSize).append("</td></tr><tr><td align='left'>").append("URL Set Size: " +
                                "</td><td align='center'>").append(uniqueUrlSize).append("</td></tr><tr><td align='left'>").
                                append("Frontier Queue Size: </td><td align='center'>").append(frontierSize)
                                .append("</td></tr><tr><td align='left'>Iteration</td><td align='center'>").append(counter_iter)
                                .append("</td></tr>");
                            mainFrame_.getInfoTextPane().setText(stringBuilder.toString());
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }

                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            double ratio = ((double) counter_iter / ITERATION_MAX);
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
            } else {
                try {
                    synchronized (this) {
                        this.wait();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }



        StringBuilder logString = new StringBuilder();
        print("\nQueue size when finished : %d", frontierQueue_.size());
        logString.append(String.format("Queue size when finished : %d\n", frontierQueue_.size()));
        print("Total links crawled: %d", crawledLinks_.size());
        logString.append(String.format("Total links crawled: %d\n", crawledLinks_.size()));
        int counter = 1;
        for (String tmpUrl : crawledLinks_.keySet()) {
            System.out.printf("%d. Link: <%s>\n", counter, tmpUrl);
            logString.append(String.format("\t%d. link: <%s>\n", counter, tmpUrl));
            counter++;
        }

        print("Flow of the links(%d):\n\n", graphOfNet_.size());
        logString.append(String.format("Flow of the links(%d): \n\n", graphOfNet_.size()));
        Set<String> keySet = graphOfNet_.keySet();
        for (String mainUrl : keySet) {
            System.out.printf("Links from <%s>\n", mainUrl);
            logString.append(String.format("Links from <%s>\n", mainUrl));
            Set<String> subKeySet = graphOfNet_.get(mainUrl).keySet();
            for (String subUrl : subKeySet) {
                System.out.printf("\t\t%s - %d\n", subUrl, graphOfNet_.get(mainUrl).get(subUrl));
                logString.append(String.format("\t\t%s - %d\n", subUrl, graphOfNet_.get(mainUrl).get(subUrl)));
            }
        }

        logger_.log(logString.toString());


    }

    private void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

    private boolean isLinkOk(String link) {
        return (link.matches("^http.*")  && !link.endsWith("pdf")
            && !link.endsWith("jpg") && !link.endsWith("png") && !link.endsWith("docx"));
    }

    private String getCoreLink(String link) {
        Pattern pattern = Pattern.compile("^http[s]?://[^/]*/");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find())
            return matcher.group(0);
        else return "";
    }


    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame_ = mainFrame;
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> getGraphOfNet_() {
        return graphOfNet_;
    }

    public void setPath_to_log(String path_to_log) {
        this.path_to_log = path_to_log;
    }

    public void setStopOrder(boolean stopOrder) {
        this.stopOrder = stopOrder;
    }

    public boolean getStopOrder() {
        return this.stopOrder;
    }

    public void setFilterKeywords_(HashMap<String, Integer> filterKeywords_) {
        this.filterKeywords_ = filterKeywords_;
    }
}
