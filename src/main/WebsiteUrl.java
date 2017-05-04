package main;


public class WebsiteUrl {
    /* Contains the original url w/out modifying it */
    private String absoluteUrl_;
    /* Contains the core link e.g., www.google.com/blah-blah/ --> www.google.com/    */
    private String coreUrl_;
    /* Contains the text which is used to represent-describe URL */
    private String descriptionOfUrl_;

    public WebsiteUrl(String absoluteUrl_, String coreUrl_, String descriptionOfUrl_) {
        this.absoluteUrl_ = absoluteUrl_;
        this.coreUrl_ = coreUrl_;
        this.descriptionOfUrl_ = descriptionOfUrl_;
    }


    /* Getters and Setters */

    public String getAbsoluteUrl_() {
        return absoluteUrl_;
    }

    public void setAbsoluteUrl_(String absoluteUrl_) {
        this.absoluteUrl_ = absoluteUrl_;
    }

    public String getCoreUrl_() {
        return coreUrl_;
    }

    public void setCoreUrl_(String coreUrl_) {
        this.coreUrl_ = coreUrl_;
    }

    public String getDescriptionOfUrl_() {
        return descriptionOfUrl_;
    }

    public void setDescriptionOfUrl_(String descriptionOfUrl_) {
        this.descriptionOfUrl_ = descriptionOfUrl_;
    }
}
