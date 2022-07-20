package Server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class SConfigs {
    @JsonProperty("address")
    String address;
    @JsonProperty("port")
    int port;
    // multicast config parameters
    @JsonProperty("mcastAddress")
    String mcastAddress;
    @JsonProperty("mcastPort")
    int mcastPort;
    // reward update timer in seconds
    @JsonProperty("rewardTimer")
    long rewardTimer;
    // backup timer in seconds
    @JsonProperty("backupTimer")
    long backupTimer;
    // author wincoins share in %
    @JsonProperty("authorShare")
    int authorShare;
    // RMI configs parameters
    @JsonProperty("rmiName")
    String rmiName;
    @JsonProperty("rmiPort")
    int rmiPort;

    public SConfigs(){

    }

    public SConfigs(String address, int port, String mcastAddress, int mcastPort, long rewardTimer, long backupTimer, int authorShare, String rmiName, int rmiPort){
        this.address = address;
        this.port = port;
        this.mcastAddress = mcastAddress;
        this.mcastPort = mcastPort;
        this.rewardTimer = rewardTimer;
        this.backupTimer = backupTimer;
        this.authorShare = authorShare;
        this.rmiName = rmiName;
        this.rmiPort = rmiPort;
    }

    public static SConfigs getConfigs(File configfile){
        SConfigs toreturn = new SConfigs();
        if(!configfile.exists()){
            System.out.println("Given ConfigFile doesnt exists, using default parameters");
            toreturn.address = "localhost";
            toreturn.port = 7775;
            toreturn.mcastAddress = "230.0.0.1";
            toreturn.mcastPort = 7776;
            toreturn.rewardTimer = 30;
            toreturn.backupTimer = 5;
            toreturn.authorShare = 75;
            toreturn.rmiName = "rmi";
            toreturn.rmiPort = 7777;
        }
        else{
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                toreturn = objectMapper.readValue(configfile, SConfigs.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toreturn;
    }
}
