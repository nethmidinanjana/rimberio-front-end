package model;

import java.io.Serializable;
import java.util.Date;

public class FeedBackItem implements Serializable {
    private String name;
    private Date dateTime;
    private int rating;
    private String feedbackTxt;

    public FeedBackItem(String name, Date dateTime, int rating, String feedbackTxt) {
        this.name = name;
        this.dateTime = dateTime;
        this.rating = rating;
        this.feedbackTxt = feedbackTxt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getFeedbackTxt() {
        return feedbackTxt;
    }

    public void setFeedbackTxt(String feedbackTxt) {
        this.feedbackTxt = feedbackTxt;
    }
}
