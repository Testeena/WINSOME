package Server;

import java.sql.Timestamp;

public class Vote {
    private String author;
    private int rate;
    private Timestamp timestamp;

    public Vote() {

    }

    public Vote(String author, int rate){
        this.author = author;
        this.rate = rate;
        this.timestamp =  new Timestamp(new java.util.Date().getTime());
    }

    public String getAuthor() {
        return author;
    }

    public int getRate() {
        return rate;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
