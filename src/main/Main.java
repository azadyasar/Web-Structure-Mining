package main;


import sun.jvm.hotspot.debugger.Page;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class Main {

    public static void main(String[] args) {

        MainFrame mainFrame = null;
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });


        /*String path = "/Users/ay/Documents/WorkSpace/Java_Workspace/PageRank/output";
        Scanner scanner = new Scanner(System.in);
        System.out.printf("Name: ");
        path = path + File.separator + scanner.nextLine();
        System.out.println("Path is: " + path);
        ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> graphOfNet = null;
        try (FileInputStream fin = new FileInputStream(path);
             ObjectInputStream oin = new ObjectInputStream(fin)) {
            graphOfNet = (ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>>) oin.readObject();
            System.out.println("Size: " + graphOfNet.size());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        PageRank pageRank = new PageRank(graphOfNet);
        pageRank.printGraphFlow();
        pageRank.calculatePageRanks();
        pageRank.printPageRanks();

        GraphVisualizer graphVisualizer = new GraphVisualizer(pageRank.getGraphOfNet_(), pageRank.getPagerankMap_(),
            "yildiz-net");

        graphVisualizer.visualize();*/


    }

}
