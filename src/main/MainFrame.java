package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by azad on 4/10/17.
 */
public class MainFrame {

    private CrawlerThreadService crawlerThreadService_;
    private PageRank pageRank_;
    private HITS hits_;

    private JPanel mainPanel_;
    private JTextField seedUrlTextField_;
    private JButton crawlButton_;
    private JButton pageRankButton;
    private JButton HITSButton;
    private JButton importLegacyGraphButton;
    private JTextField maxIterTextField_;
    private JTextField maxThreadSizeTextField_;
    private JButton graphVizButton;
    private JButton importFilterKeywordsButton;
    private JButton exportGraphButton;
    private JTextField graphVizNodeSizeTextField_;
    private JProgressBar progressBar;
    private JButton stopButton;
    private JTextPane foundUrlsTextPane;
    private JTextPane infoTextPane;
    private JButton continueCrawlingButton;
    private JFrame mainFrame_;
    private MainFrame selfPointer_;
    private JFileChooser fileChooser_;
    private File importedLegacyGraphFile_;
    private File importedFilterFile_;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> importedLegacyGraph_;
    private File jarPath_;


    public MainFrame() {

        mainFrame_ = new JFrame("Web Structural Mining");
        mainFrame_.setSize(850, 700);
        mainFrame_.setLocation(360, 164);
        mainFrame_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame_.getContentPane().add(mainPanel_);
        mainFrame_.setVisible(true);

        foundUrlsTextPane.setEditable(false);
        infoTextPane.setEditable(false);


        try {
            jarPath_ = new File(MainFrame.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
            jarPath_ = null;
        }
        for (int i = 0; i < 3; i++)
            jarPath_ = jarPath_.getParentFile();



        selfPointer_ = this;
        importLegacyGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser_ =  new JFileChooser();
                if (jarPath_ == null)
                    fileChooser_.setCurrentDirectory(new File(System.getProperty("user.home")));
                else
                    fileChooser_.setCurrentDirectory(jarPath_);

                int result = fileChooser_.showDialog(mainPanel_, "Import Legacy Graph");
                if (result == JFileChooser.APPROVE_OPTION) {
                    importedLegacyGraphFile_ = fileChooser_.getSelectedFile();
                    System.out.printf("Selected file: %s from %s-%s\n", importedLegacyGraphFile_.getName(),
                        importedLegacyGraphFile_.getAbsoluteFile(), fileChooser_.getName());
                    importedLegacyGraph_ = null;
                    try (FileInputStream fin = new FileInputStream(importedLegacyGraphFile_);
                         ObjectInputStream oin = new ObjectInputStream(fin)) {
                        importedLegacyGraph_ = (ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>) oin.readObject();
                        System.out.println("Size: " + importedLegacyGraph_.size());
                    } catch (Exception e1) {
                        System.err.println(e1.getMessage());
                        e1.printStackTrace();
                    }

                }
            }
        });

        crawlButton_.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    int thread_size = Integer.parseInt(maxThreadSizeTextField_.getText());
                    int iteration_max = Integer.parseInt(maxIterTextField_.getText());
                    String url = seedUrlTextField_.getText();
                    System.out.printf("iter:%d, thread:%d,  %s", iteration_max, thread_size, url);

                    if ( thread_size < 0 || iteration_max < 0 || !isLinkOk(url))
                        throw new NumberFormatException();
                    crawlerThreadService_ = new CrawlerThreadService(iteration_max, thread_size, url);
                    crawlerThreadService_.setMainFrame(selfPointer_);
                    if ( importedFilterFile_ != null)
                        crawlerThreadService_.setFilterKeywords_(TextAnalyser.wordToNewVector(
                            FileReaderEx.readTextFromFile(importedFilterFile_)));
                    else
                        JOptionPane.showMessageDialog(mainFrame_, "Crawling without filtering", "No Filter",
                            JOptionPane.WARNING_MESSAGE);

                    crawlerThreadService_.start();
                } catch (NumberFormatException numberFormatException) {
                    JOptionPane.showMessageDialog(mainFrame_, "Inappropriate Input!", "Input Warning",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        importFilterKeywordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser_ = new JFileChooser();
                if (jarPath_ == null)
                    fileChooser_.setCurrentDirectory(new File(System.getProperty("user.home")));
                else
                    fileChooser_.setCurrentDirectory(jarPath_);
                int result = fileChooser_.showDialog(mainPanel_, "Import Filter Keywords");
                if (result == JFileChooser.APPROVE_OPTION) {
                    importedFilterFile_ = fileChooser_.getSelectedFile();
                }
            }
        });

        pageRankButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"Legacy Graph", "Crawled Graph"};
                int n = JOptionPane.showOptionDialog(mainFrame_, "Which one of the graphs do you want to apply" +
                    "PageRank?", "Graph Picker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                options, options[0]);
                if (n == 0) {
                    if (importedLegacyGraphFile_ == null || importedLegacyGraph_ == null)
                        JOptionPane.showMessageDialog(mainFrame_, "Import legacy graph first", "No Legacy Graph",
                            JOptionPane.ERROR_MESSAGE);
                    else {
                        pageRank_ = new PageRank(importedLegacyGraph_);
                        pageRank_.setMainFrame(selfPointer_);
                        pageRank_.start();
                    }
                } else if (n == 1) {
                    if (crawlerThreadService_ == null) {
                        JOptionPane.showMessageDialog(mainFrame_, "Crawl the web first", "Graph Warning",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    pageRank_ = new PageRank(crawlerThreadService_.getGraphOfNet_());
                    pageRank_.setMainFrame(selfPointer_);
                    pageRank_.start();
                }
            }
        });

        exportGraphButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ( crawlerThreadService_ == null) {
                    JOptionPane.showMessageDialog(mainFrame_, "Crawl the web first", "Graph Warning",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (crawlerThreadService_.getGraphOfNet_().size() == 0) {
                    JOptionPane.showMessageDialog(mainFrame_, "Graph is empty", "Empty Graph",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                fileChooser_ = new JFileChooser();
                if (jarPath_ == null)
                    fileChooser_.setCurrentDirectory(new File(System.getProperty("user.home")));
                else
                    fileChooser_.setCurrentDirectory(jarPath_);
                fileChooser_.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
                int result = fileChooser_.showDialog(mainPanel_, "Export Folder");
                if (result == JFileChooser.APPROVE_OPTION) {
                    File folder = fileChooser_.getSelectedFile();
                    File outputGraphFile = new File(folder.getPath() + File.separator + "Graph_" + (new Date().getTime()));
                    System.out.println(outputGraphFile);
                    System.out.println(File.separator);
                    try (FileOutputStream fileOutputStream = new FileOutputStream(outputGraphFile);
                         ObjectOutputStream out = new ObjectOutputStream(fileOutputStream)) {
                        out.writeObject(crawlerThreadService_.getGraphOfNet_());
                    } catch (Exception e1) {
                        System.err.println(e1.getMessage());
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(mainFrame_, "Error occured while writing to file",
                            "IO Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        graphVizButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /*if ( crawlerThreadService_ == null && importedLegacyGraph_ == null) {
                    JOptionPane.showMessageDialog(mainFrame_, "Crawl the web first or import graph",
                        "Graph Warning", JOptionPane.WARNING_MESSAGE);
                    return;
                } else if (crawlerThreadService_.getGraphOfNet_().size() == 0 && importedLegacyGraph_.size() == 0) {
                    JOptionPane.showMessageDialog(mainFrame_, "Graph is empty", "Empty Graph",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }*/
                if ( pageRank_ == null && hits_ == null) {
                    JOptionPane.showMessageDialog(mainFrame_, "Apply PageRank or HITS first", "Unused Algorithms",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int nodeSize = 30;
                try {
                    nodeSize = Integer.parseInt(graphVizNodeSizeTextField_.getText());
                } catch (NumberFormatException exception) {
                    JOptionPane.showMessageDialog(mainFrame_, "Inappropriate Input!", "Input Warning",
                        JOptionPane.ERROR_MESSAGE);
                }

                if (nodeSize <= 0 ) {
                    JOptionPane.showMessageDialog(mainFrame_, "Number of nodes cannot be negative. Default: 30",
                        "Input Warning", JOptionPane.ERROR_MESSAGE);
                    nodeSize = 30;
                }

                Object[] options = {"PageRank", "HITS"};
                int n = JOptionPane.showOptionDialog(mainFrame_, "Which algorithm's result do you want to visualize",
                    "AlgorithmVisualizer", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, options[0]);

                if (n == 0) {
                    if (pageRank_ == null) {
                        JOptionPane.showMessageDialog(mainFrame_, "Apply PageRank first", "Unused Algorithms",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    GraphVisualizer graphVisualizer = new GraphVisualizer(pageRank_.getGraphOfNet_(), pageRank_.getPagerankMap_(),
                        "yildiz-net");
                    graphVisualizer.setFirst_n_urls(nodeSize);
                    graphVisualizer.visualize();
                } else if (n == 1) {
                    if (hits_ == null) {
                        JOptionPane.showMessageDialog(mainFrame_, "Apply HITS first", "Unused Algorithms",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    GraphVisualizer graphVisualizer = new GraphVisualizer(hits_.getGraphOfNet(), hits_.getAuthMap(),
                        "yildiz-net");
                    graphVisualizer.setFirst_n_urls(nodeSize);
                    graphVisualizer.visualize();
                }
            }
        });

        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (crawlerThreadService_ != null) {
                    crawlerThreadService_.setStopOrder(true);
                }
            }
        });

        HITSButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object[] options = {"Legacy Graph", "Crawled Graph"};
                int n = JOptionPane.showOptionDialog(mainFrame_, "Which one of the graphs do you want to apply" +
                        "PageRank?", "Graph Picker", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
                    options, options[0]);
                if (n == 0) {
                    if (importedLegacyGraphFile_ == null || importedLegacyGraph_ == null)
                        JOptionPane.showMessageDialog(mainFrame_, "Import legacy graph first", "No Legacy Graph",
                            JOptionPane.ERROR_MESSAGE);
                    else {
                        hits_ = new HITS(importedLegacyGraph_);
                        hits_.setMainFrame(selfPointer_);
                        hits_.start();
                    }
                } else if (n == 1) {
                    if (crawlerThreadService_ == null) {
                        JOptionPane.showMessageDialog(mainFrame_, "Crawl the web first", "Graph Warning",
                            JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    hits_ = new HITS(crawlerThreadService_.getGraphOfNet_());
                    hits_.setMainFrame(selfPointer_);
                    hits_.start();

                }
            }
        });

        continueCrawlingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (crawlerThreadService_ == null) {
                    JOptionPane.showMessageDialog(mainFrame_, "Start crawling first", "Crawler Warning",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                synchronized (crawlerThreadService_) {
                    crawlerThreadService_.setStopOrder(false);
                    crawlerThreadService_.notify();
                }
            }
        });
    }

    private boolean isLinkOk(String link) {
        return (link.matches("^http.*") && !link.endsWith("pdf")
            && !link.endsWith("jpg") && !link.endsWith("png") && !link.endsWith("docx"));
    }

    public void updateCrawledLinks(ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                StringBuilder stringBuilder = new StringBuilder("<html><table>");
                Set<String> keySet = graphOfNet.keySet();
                int counter = 1;
                for (String keyTmp : keySet)
                    stringBuilder.append("<tr><td align='right'>").append(counter++).append("</td><td>")
                        .append(keyTmp).append("</td></tr>");
                /*crawledUniqueUrlTextArea.setText(stringBuilder.toString());
                mainPanel_.updateUI();*/
                foundUrlsTextPane.setText(stringBuilder.toString());
            }
        });

    }

    public JTextPane getInfoTextPane() { return this.infoTextPane; }
    public JProgressBar getProgressBar() { return this.progressBar; }
    public JTextPane getFoundUrlsTextPane() { return  this.foundUrlsTextPane; }
}
