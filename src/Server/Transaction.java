package Server;

import java.sql.Timestamp;

public class Transaction {
    private final double amount;
    private final Timestamp timestamp;

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
