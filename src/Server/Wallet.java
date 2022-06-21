package Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Wallet {
    private Double balance;
    private final ConcurrentLinkedQueue<Transaction> transactions;

    public Wallet(){
        this.balance = (double)0;
        this.transactions = new ConcurrentLinkedQueue<>();
    }

    public Double getBalance(){
        return this.balance;
    }

    public Double getBitcoinBalance(){
        double bitcoinvalue = 0;

        try {
            URL url = new URL("https://www.random.org/decimal-fractions/?num=1&dec=10&col=1&format=plain&rnd=new");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            if(connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                BufferedReader inbuff = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                bitcoinvalue = Double.parseDouble(inbuff.readLine());
                inbuff.close();
            }
            else{
                System.err.println("HTTP response error: random.org unreachable");
                return (double)-1;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this.balance * bitcoinvalue;
    }

    public ConcurrentLinkedQueue<Transaction> getTransactions() {
        return transactions;
    }

    public void addTransaction(double amount){
        this.balance += amount;
        this.transactions.add(new Transaction(amount));
    }
}
