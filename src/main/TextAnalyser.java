package main;


import java.util.HashMap;

public class TextAnalyser {

    private HashMap<String, Integer> histMap_;

    public TextAnalyser() {
        histMap_ = new HashMap<>();
    }


    /* Given a text, words are added to hashmap or in case of hit their frequency is incremented */
    public void wordToVector(String text) {
        String word_list[] = text.split(" ");
        System.out.printf("%d: ", word_list.length);
        for (String word : word_list) {
//            System.out.printf("%s,\t", word);

            if (!histMap_.containsKey(word.toLowerCase()))
                histMap_.put(word.toLowerCase(), 1);
            else {
                int freq = histMap_.get(word.toLowerCase());
                histMap_.put(word.toLowerCase(), freq + 1);
            }

        }
        System.out.println();
    }

    public static HashMap<String, Integer> wordToNewVector(String text) {
        HashMap<String, Integer> result = new HashMap<>();
        String word_list[] = text.split(" ");
        for (String word : word_list) {

            if (!result.containsKey(word.toLowerCase()))
                result.put(word.toLowerCase(), 1);
            else {
                int freq = result.get(word.toLowerCase());
                result.put(word.toLowerCase(), freq + 1);
            }

        }
        return result;
    }

    /* Getter and Setters */

    public HashMap<String, Integer> getHistMap_() {
        return histMap_;
    }

    public void setHistMap_(HashMap<String, Integer> histMap_) {
        this.histMap_ = histMap_;
    }



}
