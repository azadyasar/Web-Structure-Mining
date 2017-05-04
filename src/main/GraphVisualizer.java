package main;

import org.graphstream.graph.*;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.view.Viewer;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GraphVisualizer{

    private HashMap<String, HashMap<String, Integer>> graphOfNet_;
    private HashMap<String, Double> pageranks_;
    private HashMap<String, HashMap<String, Integer>> eliminatedGraphOfNet_;
    private HashMap<String, Double> eliminatedPageRanks_;
    private String nameOfNetwork_;
    /* Contains the threshold that will be used when comparing two successive URL's in order to
    decide whether decrease the size of network or not
     */
    private final double THRESHOLD_PAGERANK = 0.2;
    private  int first_n_urls;

    public GraphVisualizer() {}

    public GraphVisualizer(HashMap<String, HashMap<String, Integer>> graphOfNet, HashMap<String, Double> pageranks,
                           String nameOfNetwork) {
        this.graphOfNet_ = graphOfNet;
        this.pageranks_ = pageranks;
        this.nameOfNetwork_ = nameOfNetwork;
        first_n_urls = 30;
    }

    public void visualize() {
        constructGraph();
    }

    private void constructGraph() {
        Graph graph = new MultiGraph(nameOfNetwork_);
        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.setAutoCreate(true);
        graph.setStrict(false);
        Viewer viewer = graph.display();
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.HIDE_ONLY);
        graph.addAttribute("layout.weight", 100000);
        eliminateGraphOfNet();
        Map<String, Double> sortedPageRanks;
        sortedPageRanks = MapUtil.sortByValue(eliminatedPageRanks_);
        Set<String> keySet = sortedPageRanks.keySet();
        int nodeSize = 125;
        int decrease = 25;
        Random random = new Random();
        double lastPageRank = Double.MAX_VALUE;
        for (String urlTmp : keySet) {
            String coreUrlTmp = getCoreLink(urlTmp);
            graph.addNode(coreUrlTmp);
            graph.getNode(coreUrlTmp).setAttribute("ui.label", graph.getNode(coreUrlTmp).getId());
            graph.getNode(coreUrlTmp).setAttribute("ui.style",  " text-font:    arial;" + " text-size:    11;" +
                 " text-color:   rgb(20,100,20);" + " text-style:   bold;" +"size:" + nodeSize + "px; fill-color:" +
                "rgb(" + random.nextInt(255) + "," + random.nextInt(255) + "," +
                random.nextInt(255) + ");");
            if (lastPageRank - pageranks_.get(urlTmp) > THRESHOLD_PAGERANK && nodeSize > 20) {
                nodeSize -= decrease;
                decrease *= 0.75;
            }
            Set<String> refUrlSet = eliminatedGraphOfNet_.get(urlTmp).keySet();
            for (String subUrlTmp : refUrlSet) {
                graph.addEdge(coreUrlTmp + "-" + getCoreLink(subUrlTmp), coreUrlTmp, getCoreLink(subUrlTmp), true);
            }
        }

        for (Edge edge : graph.getEdgeSet())
            edge.setAttribute("layout.weight", 25);
    }

    private void eliminateGraphOfNet() {
        eliminatedGraphOfNet_ = new HashMap<>();
        eliminatedPageRanks_ = new HashMap<>();
        HashMap<String, Integer> urlDomain = new HashMap<>();
        int counter = 0;
        Map<String, Double> sortedPageRanks = new LinkedHashMap<>();
        sortedPageRanks = MapUtil.sortByValue(pageranks_);
        Set<String> keySet = sortedPageRanks.keySet();
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext() && counter++ < first_n_urls)
            urlDomain.put(iterator.next(), 1);
        iterator = keySet.iterator();
        counter = 0;
        while (iterator.hasNext() && counter++ < first_n_urls) {
            String url = iterator.next();
            eliminatedPageRanks_.put(url, pageranks_.get(url));
            eliminatedGraphOfNet_.put(url, new HashMap<>());
            Set<String> subUrlSet = graphOfNet_.get(url).keySet();
            for (String subUrlTmp : subUrlSet)
                if (urlDomain.containsKey(subUrlTmp))
                    eliminatedGraphOfNet_.get(url).put(subUrlTmp, 1);
        }
    }

    private  String getCoreLink(String link) {
        Pattern pattern = Pattern.compile("www.[^.]*.[^.]*.");
        Matcher matcher = pattern.matcher(link);
        String result;
        if (matcher.find()) {
            result = matcher.group(0);
            return result.substring(4, result.length()-1);
        }

        return link;
    }


    /* Getters and Setters */

    public HashMap<String, HashMap<String, Integer>> getGraphOfNet_() {
        return graphOfNet_;
    }

    public void setGraphOfNet_(HashMap<String, HashMap<String, Integer>> graphOfNet_) {
        this.graphOfNet_ = graphOfNet_;
    }

    public HashMap<String, Double> getPageranks_() {
        return pageranks_;
    }

    public void setFirst_n_urls(int first_n_urls) {
        this.first_n_urls = first_n_urls;
    }

    public void setPageranks_(HashMap<String, Double> pageranks_) {
        this.pageranks_ = pageranks_;
    }

    protected String styleSheet =
        "node {" +
            "   fill-color:black;" +
            " size: 20px;" +
            "}" +
            "node.ten {" +
            "fill-color: red;" +
            "size: 80px;" +
            "}" +
            "node.nine {" +
            "fill-color: orange;" +
            "size: 70px;" +
            "}" +
            "node.eight {" +
            "fill-color: yellow;" +
            "size: 60px;" +
            "}" +
            "node.seven {" +
            "fill-color: blue;" +
            "size: 50px;" +
            "}" +
            "node.six {" +
            "fill-color: pink;" +
            "size: 40px;" +
            "}" +
            "node.five {" +
            "fill-color: magenta;" +
            "size: 30px;" +
            "}";
}
