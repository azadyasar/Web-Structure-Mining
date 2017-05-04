package main;


import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler extends Thread  {

    private static final int MAX_URL_LENGTH = 50;
    private String mainUrl_;
    private ConcurrentLinkedQueue<String> frontierQueue_;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet_;
    private ConcurrentHashMap<String, Integer> crawledLinks_;
    private HashMap<String, Integer> filterKeywords_;
    private Logger logger_;

    public Crawler(String mainUrl, ConcurrentLinkedQueue frontierQueue,
                   ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet,
                   ConcurrentHashMap<String, Integer> crawledLinks) {
        this.mainUrl_ = mainUrl;
        this.frontierQueue_ = frontierQueue;
        this.graphOfNet_ = graphOfNet;
        this.crawledLinks_ = crawledLinks;
        this.logger_ = Logger.getLogger();
    }



    @Override
    public void run() {
        StringBuilder logString = new StringBuilder();
        try {
            if (isLinkOk(getCoreLink(mainUrl_)) && isLinkOk(mainUrl_) && !crawledLinks_.containsKey(mainUrl_)
                && !frontierQueue_.contains(mainUrl_)) {
                Document doc = Jsoup.connect(mainUrl_).timeout(10000).validateTLSCertificates(false).get();
                Elements extractedLinks = doc.select("a[href]");
                mainUrl_ = modifyUrl(mainUrl_);
                graphOfNet_.putIfAbsent(getCoreLink(mainUrl_), new ConcurrentHashMap<>());
                /*if (crawledLinks_.get(mainUrl_) != null) {
                    System.out.printf("@@@@ %s was already crawled..\n", mainUrl_);
                }*/
                crawledLinks_.put(mainUrl_, 1);
                crawledLinks_.put(mainUrl_+"/", 1);
                for (Element subUrlTmp : extractedLinks) {
                    String subUrl = subUrlTmp.attr("abs:href").replaceAll("https", "http").replaceAll("www2", "www");
                    if (isLinkOk(subUrl)) {
                        synchronized (crawledLinks_) {
                            synchronized (frontierQueue_) {
                                if (!crawledLinks_.containsKey(subUrl) && !frontierQueue_.contains(subUrl)) {
                                    frontierQueue_.add(subUrl);
//                            System.out.printf("<%s> added to frontier (%s)\n", subUrl, this.toString());
                                    logString.append(String.format("\t<%s> added to frontier (%s)\n", subUrl, this.toString()));
                                }
                            }
                        }
                        /*else if (crawledLinks_.containsKey(subUrl))
                            System.out.printf("@@@@crawledLinks_ contains (%s)\n", subUrl);
                        else
                            System.out.printf("####Frontier already contains (%s)\n", subUrl);*/

                        if (isLinkOk(getCoreLink(subUrl)) && getCoreLink(subUrl).compareTo(getCoreLink(mainUrl_)) != 0) {
                            String coreMainUrl = getCoreLink(mainUrl_);
                            String coreSubUrl = getCoreLink(subUrl);
                            if (graphOfNet_.get(coreMainUrl).get(coreSubUrl) == null) {
                                graphOfNet_.get(coreMainUrl).put(coreSubUrl, 1);
                                /*System.out.printf("Added link flow from %s to %s\n", coreMainUrl,
                                    coreSubUrl);*/
//                                logString.append(String.format("\tAdded link flow from %s to %s\n", coreMainUrl, coreSubUrl));
                            } else {
                                int prev = graphOfNet_.get(coreMainUrl).get(coreSubUrl) + 1;
                                graphOfNet_.get(coreMainUrl).put(coreSubUrl, prev);
//                                System.out.printf("Increased link flow from %s to %s = %d\n", coreMainUrl,
//                                    coreSubUrl, prev);
                                /*logString.append(String.format("\tIncreased link flow from %s to %s = %d\n",
                                    coreMainUrl, coreSubUrl, prev));*/
                            }
                        }
                    }
                }
            } else if ( !isLinkOk(getCoreLink(mainUrl_)) || !isLinkOk(mainUrl_) ){
//                System.out.printf("%s is not ok\n", mainUrl_);
                logString.append(String.format("%s is not ok\n", mainUrl_));
            } else if (crawledLinks_.containsKey(mainUrl_) )
                logString.append(String.format("crawledLinks contains %s\n", mainUrl_));
            else if  (frontierQueue_.contains(mainUrl_))
                logString.append(String.format("frontier contains %s\n", mainUrl_));


        } catch(MalformedURLException e){
            System.err.println(e.getMessage());
            System.out.printf("Exception occured for link %s\n", mainUrl_);
            logString.append(String.format("Exception occured for link %s\n", mainUrl_) + " " + e.getMessage() + "\n");
        } catch(UnsupportedMimeTypeException e){
            System.err.println(e.getMessage());
            System.out.printf("Exception occured for link %s\n", mainUrl_);
            logString.append(String.format("Exception occured for link %s\n", mainUrl_) + " " + e.getMessage() + "\n");
        } catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
            System.out.printf("Exception occured for link %s\n", mainUrl_);
            logString.append(String.format("Exception occured for link %s\n", mainUrl_) + " " + e.getMessage() + "\n");
        }

        /*System.out.printf("Crawled from (%s)\n", this.toString());
        logString.append(String.format("<<<<Crawled from (%s)>>>>\n\n", this.toString()));*/
        logger_.log(logString.toString());
    }

    private String modifyUrl(String url) {
        return url.replace("www2", "www");
    }

    private boolean isLinkOk(String link) {
        return (link.matches("^http[s]?.//.*") && containsKeyword(link) && !link.endsWith("pdf")
            && !link.endsWith("jpg") && !link.endsWith("png") && !link.endsWith("docx") && !link.endsWith("doc")
            && !link.endsWith("xls") && !link.endsWith("xlsx") && !link.endsWith("DOCX") && !link.endsWith("#")
            && (link.length() < 128));
    }

    private String getCoreLink(String link) {
        if (!link.contains("www")) {
            if (link.startsWith("https"))
                link = "http://www." + link.substring(8);
            else if (link.startsWith("http"))
                link = link.substring(0, 7) + "www." + link.substring(7);
        }

        link = link.replace("www2", "www").replace("#", "/");
        Pattern pattern = Pattern.compile("^http[s]?://[^/]*/");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            String result =  matcher.group(0).toLowerCase();
            if (result.contains("@") || !(result.length() < MAX_URL_LENGTH))
                return "";
            else
                return result;
        }
        pattern = Pattern.compile("^http[s]?://[^.]*.[^.]*.[^.]*.[^.]*...");
        matcher = pattern.matcher(link);
        if (matcher.find()) {
            String result = (matcher.group(0) + "/").toLowerCase();
            if (result.contains("@") || !(result.length() < MAX_URL_LENGTH))
                return "";
            else
                return result;
        }
        else return "";
    }

    private boolean containsKeyword(String link) {
        if (filterKeywords_.size() == 0)
            return true;
        Set<String> keyWordSet = filterKeywords_.keySet();
        for (String keyWordTmp : keyWordSet)
            if (link.contains(keyWordTmp))
                return true;
        return false;
    }

    @Override
    public String toString() {
        return mainUrl_;
    }

    public void setFilterKeywords(HashMap<String, Integer> filterKeywords) {
        this.filterKeywords_ = filterKeywords;
    }
}
