package model;

import java.io.Serializable;
import java.util.Date;

public class Notification implements Serializable {
    private Date dateTime;
    private String message;

    public Notification(Date dateTime, String message) {
        this.dateTime = dateTime;
        this.message = message;
    }

    public Date getDateTime() {
        return dateTime;
    }

    public void setDateTime(Date dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
