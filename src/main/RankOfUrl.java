package main;



public class RankOfUrl implements Comparable<RankOfUrl>{

    private Double pageRank_;
    /* Importance used for setting size of the node when visualizing */
    private int importance;

    public RankOfUrl(Double pageRank_, int importance) {
        this.pageRank_ = pageRank_;
        this.importance = importance;
    }


    /* Getters and Setters */

    public Double getPageRank_() {
        return pageRank_;
    }

    public void setPageRank_(Double pageRank_) {
        this.pageRank_ = pageRank_;
    }

    public int getImportance() {
        return importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }


    public int compareTo(RankOfUrl rankOfUrl) {
        if (this.pageRank_ > rankOfUrl.getPageRank_())
            return 1;
        else if (this.pageRank_ == rankOfUrl.getPageRank_())
            return 0;
        return -1;
    }
}
