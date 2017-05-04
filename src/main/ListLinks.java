package main;

import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.helper.StringUtil;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ListLinks {

    private static Queue<String> frontierQueue = new LinkedList<>();
    private static LinkedHashSet<String> markedLinks = new LinkedHashSet<>();
    private static HashMap<String, Integer> crawledLinks = new HashMap<>();
    private static HashMap<String, HashMap<String, Integer>> graphOfNet = new HashMap<>();
    private static final int NUMBER_OF_LINKS = 125;
    private static final int ITERATION_MAX = 1000;

    public static void main(String[] args) throws IOException {
        /*Validate.isTrue(args.length == 1, "usage: supply seedUrl to fetch");
        String seedUrl = args[0];
        System.out.printf("Fetching %s...", seedUrl);*/

        Scanner scanner = new Scanner(System.in);
        System.out.printf("Url: ");
        String seedUrl = scanner.nextLine();
        Document doc;
        if (isLinkOk(seedUrl)) {
            if (!seedUrl.endsWith("/"))
                seedUrl = seedUrl + "/";
        seedUrl = seedUrl.replace(" ", "");
        frontierQueue.add(seedUrl);
        }
        else {
            System.err.printf("Provide a proper seedUrl.\n");
            return;
        }

        markedLinks.add(seedUrl);
        boolean listFilled = false;
        int counter_iter = 0;
        //while (!frontierQueue.isEmpty() && !listFilled) {
        while (counter_iter++ < ITERATION_MAX) {
            String nextUrl = frontierQueue.remove();
            System.out.printf("At <%s>\n", nextUrl);
            crawledLinks.put(nextUrl, 1);

            //if (markedLinks.size() < NUMBER_OF_LINKS) {
            try {
                if (isLinkOk(getCoreLink(nextUrl))) {
                    doc = Jsoup.connect(nextUrl).timeout(10000).validateTLSCertificates(false).get();
                    Elements extractedLinks = doc.select("a[href]");
                    Iterator<Element> elementIterator = extractedLinks.iterator();

                    graphOfNet.putIfAbsent(getCoreLink(nextUrl), new HashMap<String, Integer>());

                    while (elementIterator.hasNext()) {
                        Element link = elementIterator.next();
                        String subUrl = link.attr("abs:href");
                        if (isLinkOk(subUrl)) {
                            if (!crawledLinks.containsKey(subUrl)) {
                                frontierQueue.add(subUrl);
                                System.out.printf("<%s> added to frontier\n", subUrl);
                            }
                            if (isLinkOk(getCoreLink(subUrl)) && getCoreLink(subUrl).compareTo(getCoreLink(nextUrl)) != 0) {
                                markedLinks.add(getCoreLink(subUrl));
                                if (graphOfNet.get(getCoreLink(nextUrl)).get(getCoreLink(subUrl)) == null) {
                                    graphOfNet.get(getCoreLink(nextUrl)).put(getCoreLink(subUrl), 1);
                                    System.out.printf("Added link flow from %s to %s\n", getCoreLink(nextUrl),
                                        getCoreLink(subUrl));
                                }
                                else {
                                    int prev = graphOfNet.get(getCoreLink(nextUrl)).get(getCoreLink(subUrl)) + 1;
                                    graphOfNet.get(getCoreLink(nextUrl)).put(getCoreLink(subUrl), prev);
                                    System.out.printf("Increased link flow from %s to %s = %d\n", getCoreLink(nextUrl),
                                        getCoreLink(subUrl), prev);
                                }
                            }

                        } else if (crawledLinks.containsKey(subUrl))
                            System.out.printf("<%s> was already crawled..\n", subUrl);
                    }
                }
                else
                    System.out.printf("%s is not ok\n", getCoreLink(nextUrl));

            }catch (MalformedURLException e) {
                System.err.println(e.getMessage());
                System.out.printf("Exception occured for link %s\n", nextUrl);
            } catch (UnsupportedMimeTypeException e) {
                System.err.println(e.getMessage());
                System.out.printf("Exception occured for link %s\n", nextUrl);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
                System.out.printf("Exception occured for link %s\n", nextUrl);
            }
            /*} else {
                listFilled = true;
                System.out.println("Marked list filled...");
            }*/
//            System.out.printf("Marked List Size :%d\n", markedLinks.size());
            System.out.printf("Flow size: %d\nIter: %d\nFrontier size:%d\nCrawled so far: %d\n", graphOfNet.size(),
                counter_iter, frontierQueue.size(), crawledLinks.size());
        }
        print("Queue size when finished : %d", frontierQueue.size());
        print("Total links downloaded: %d", markedLinks.size());
        int counter = 1;
        for (String tmpUrl : markedLinks) {
            System.out.printf("%d. Link: <%s>\n", counter, tmpUrl);
            counter++;
        }

        print("Flow of the links(%d):\n\n", graphOfNet.size());
        Set<String> keySet = graphOfNet.keySet();
        for (String mainUrl : keySet) {
            System.out.printf("Links from <%s>\n", mainUrl);
            Set<String> subKeySet = graphOfNet.get(mainUrl).keySet();
            for (String subUrl : subKeySet)
                System.out.printf("\t\t%s - %d\n", subUrl, graphOfNet.get(mainUrl).get(subUrl));
        }

        /*Document doc = Jsoup.connect(seedUrl).timeout(10000).validateTLSCertificates(false).get();
        Elements links = doc.select("a[href]");

        print("\n Links from %s: (%d)", seedUrl, links.size());
        Set<String> linkSet = new HashSet<>();
        for (Element link : links) {
            print("\t * a: <%s> (%s)", link.attr("abs:href"), link.text());
            if (isLinkOk(link.attr("abs:href")))
                linkSet.add(getCoreLink(link.attr("abs:href")));
        }
        System.out.println();
        System.out.printf("Set size%d\n", linkSet.size());
        for (String name : linkSet)
            System.out.println(name);
        for (String suburl : linkSet) {
            if (isLinkOk(suburl)) {
                try {
                    doc = Jsoup.connect(suburl).timeout(10000).validateTLSCertificates(false).get();
                    links = doc.select("a[href");
                    print("\n Links from %s: (%d)", suburl, links.size());
                    for (Element link : links) {
                        print("\t * a: <%s> (%s)", link.attr("abs:href"), link.text());
                    }
                } catch (MalformedURLException e) {
                    System.err.println(e.getMessage());
                    System.out.printf("Exception occured for link %s\n", suburl);
                } catch (UnsupportedMimeTypeException e) {
                    System.err.println(e.getMessage());
                    System.out.printf("Exception occured for link %s\n", suburl);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                    System.out.printf("Exception occured for link %s\n", suburl);
                }
            }

        }*/


    }

    private static String trim(String s, int width) {
        if (s.length() > width)
            return s.substring(0, width-1) + ".";
        else
            return s;
    }

    private static boolean isLinkOk(String link) {
        return (link.matches("^http.*") && link.contains("yildiz") && !link.endsWith("pdf")
            && !link.endsWith("jpg") && !link.endsWith("png") && !link.endsWith("docx"));
    }

    private static String getCoreLink(String link) {
        Pattern pattern = Pattern.compile("^http[s]?://[^/]*/");
        Matcher matcher = pattern.matcher(link);
        if (matcher.find())
            return matcher.group(0);
        else return "";
    }

    private static void print(String msg, Object... args) {
        System.out.println(String.format(msg, args));
    }

}
