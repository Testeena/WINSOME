package Server;

import java.sql.Timestamp;

public class Transaction {
    private double amount;
    private Timestamp timestamp;

    public Transaction(){

    }

    public Transaction(double amount){
        this.amount = amount;
        this.timestamp = new Timestamp(new java.util.Date().getTime());
    }

    public double getAmount() {
        return amount;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
}
