package main;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class FileReaderEx {

    private File folder_;
    private String charset_;
    private File fileList_[];
    private ArrayList<String> stopwordList_;

    public FileReaderEx() {
        charset_ = "UTF-8";
        stopwordList_ = new ArrayList<>();
    }

    public FileReaderEx(File folder) {
        this.folder_ = folder;
        this.charset_ = "UTF-8";
        if (folder.isFile())
            System.err.println("Given path is a file, expected folder");
        else
            fileList_ = folder_.listFiles();
        stopwordList_ = new ArrayList<>();
    }

    public FileReaderEx(File folder, String charset) {
        this.folder_ = folder;
        this.charset_ = charset;
        if (folder.isFile())
            System.err.println("Given path is a file, expected folder");
        else
            fileList_ = folder_.listFiles();
        stopwordList_ = new ArrayList<>();
    }



    public static String readTextFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream inputStream = new FileInputStream(file);
               Scanner scanner = new Scanner(inputStream)){
            while (scanner.hasNextLine()) {
                stringBuilder.append(scanner.nextLine() + " ");
            }

        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            return "";
        }
        return stringBuilder.toString().toLowerCase().replaceAll("  ", " ");
    }

    public String readTextFilesFromFolder(File folder) {
        System.out.println("Reading .txt files from " + folder.getName());
        StringBuilder stringBuilder = new StringBuilder();
        boolean percents[] = new boolean[4];
        for (boolean bool : percents)
            bool = false;
        if (!folder.isFile()) {
            File fileList[] = folder.listFiles();
            int size = fileList.length;
            float counter = 0;
            for (File file : fileList) {
                counter++;
                if (file.isFile() && file.getName().endsWith(".txt"))
                    stringBuilder.append(readTextFromFile(file));
                if ( (counter/size) >= 0.99 && !percents[0]) {
                    System.out.printf("---->100%%");
                    percents[0] = true;
                }
                else if ( (counter/size) >= 0.75 && !percents[1]) {
                    System.out.printf("---->75%%");
                    percents[1] = true;
                }
                else if ( (counter/size) >= 0.5 && !percents[2]) {
                    System.out.printf("---->50%%");
                    percents[2] = true;
                }
                else if ( (counter/size) >= 0.25 && !percents[3]) {
                    System.out.printf("---->25%%");
                    percents[3] = true;
                }
            }
        } else {
            System.err.println("Given file path instead of folder path\n Returning from readTextFilesFromFolder");
            return "";
        }
        System.out.println();
        return stringBuilder.toString();
    }

    /* Given file and stopword list, function reads the file and removes stopwords,
     * then writes the new text into file */
    public void cutOffStopwordsFromFile(File file) {
        StringBuilder stringBuilder = new StringBuilder();

        try (FileInputStream inputStream = new FileInputStream(file);
                Scanner scanner = new Scanner(inputStream, this.charset_)) {
            while (scanner.hasNextLine())
                stringBuilder.append(scanner.nextLine() + " ");
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
            return;
        }
        System.out.println("Text: " + stringBuilder.toString());
        String text = stringBuilder.toString().replaceAll("  ", " ");
        String[] word_list = text.split(" ");
        stringBuilder = new StringBuilder();
        for (String word : word_list) {
            word = word.toLowerCase();
            if (!isStopWord(word)) {
                word = modifyWord(word);
                System.out.printf("Writing -%s-\n", word);
                stringBuilder.append(word + " ");
            }
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(stringBuilder.toString());
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }

    }

    public static synchronized void writeToFile(File file, String doc) {
//        System.out.printf("Writing to file %s...\n", file.getName());
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true))) {
            bufferedWriter.write(doc);
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    private String modifyWord(String word) {
        String result = word.replaceAll(">", "");
        for (int i = 0; i < 9; i++) {
            result = result.replaceAll(Integer.toString(i), "");
        }
        return result;
    }

    private boolean isStopWord(String word) {
        for (int i = 0; i < stopwordList_.size(); i++) {
            if (word.contains(stopwordList_.get(i)))
                return true;
        }
        return false;
    }

    /* Given a folder all the .txt files are applied the cutOffStopWordsFromFile method */
    public void cutOffStopWordsFromFolder(File folder) {
        File fileList[] = folder.listFiles();
        int size = fileList.length;
        boolean percents[] = new boolean[4];
        for (boolean bool : percents)
            bool = false;
        float counter = 0;
        System.out.printf("Removing stopwords from %s\n", folder.getName());
        for (File file : fileList) {
            counter++;
            if (file.isFile() && file.getName().endsWith(".txt")) {
//                System.out.println("Clearing " + file.getName() + "...");
                cutOffStopwordsFromFile(file);
            }
            if ( (counter/size) >= 0.99 && !percents[0]) {
                System.out.printf("---->100%%");
                percents[0] = true;
            }
            else if ( (counter/size) >= 0.75 && !percents[1]) {
                System.out.printf("---->75%%");
                percents[1] = true;
            }
            else if ( (counter/size) >= 0.5 && !percents[2]) {
                System.out.printf("---->50%%");
                percents[2] = true;
            }
            else if ( (counter/size) >= 0.25 && !percents[3]) {
                System.out.printf("---->25%%");
                percents[3] = true;
            }
        }
        System.out.println();
    }

    public String cutOffStopWordsFromString(String text) {
        String words[] = text.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        for (String word : words) {
            word = word.toLowerCase();
            if (!stopwordList_.contains(word) && !word.contains("<") && !word.contains(">"))
                stringBuilder.append(word + " ");
        }
        return stringBuilder.toString();
    }

    /* Get stopwords from given file */
    public void acquireStopwords(File file) {
        String text = readTextFromFile(file);
        stopwordList_ = new ArrayList<>(Arrays.asList(text.split(" ")));
        System.out.printf("Got stopwords: %s\n", stopwordList_.toString());
    }


    /* Given a directory cleans what is inside it */
    public void cleanFolder(File folder) {
        System.out.printf("Cleaning folder %s\n", folder.getName());
        for (File file : folder.listFiles()) {
            if (file.isDirectory())
                cleanFolder(file);
            else
                file.delete();
        }
    }


    /* Getter and Setters */


    public File getFolder_() {
        return folder_;
    }

    public void setFolder_(File folder_) {
        this.folder_ = folder_;
    }

    public String getCharset_() {
        return charset_;
    }

    public void setCharset_(String charset_) {
        this.charset_ = charset_;
    }

    public File[] getFileList_() {
        return fileList_;
    }

    public void setFileList_(File[] fileList_) {
        this.fileList_ = fileList_;
    }

    public ArrayList<String> getStopwordList_() {
        return stopwordList_;
    }

    public void setStopwordList_(ArrayList<String> stopwordList_) {
        this.stopwordList_ = stopwordList_;
    }
}
