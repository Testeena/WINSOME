package Client;

import Server.Colors;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

public class CConfigs {
    @JsonProperty("address")
    String address;
    @JsonProperty("port")
    int port;
    @JsonProperty("rmiName")
    String rmiName;
    @JsonProperty("rmiPort")
    int rmiPort;
    @JsonProperty("mcastAddress")
    String mcastAddress;
    @JsonProperty("mcastPort")
    int mcastPort;

    public static CConfigs getConfigs(File configfile){
        CConfigs toreturn = new CConfigs();
        if(!configfile.exists()){
            System.out.println(Colors.YELLOW + "Given ConfigFile doesnt exists, using default parameters" + Colors.RESET);
            toreturn.address = "localhost";
            toreturn.port = 7775;
            toreturn.mcastAddress = "230.0.0.1";
            toreturn.mcastPort = 7776;
            toreturn.rmiName = "rmi";
            toreturn.rmiPort = 7777;
        }
        else{
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                toreturn = objectMapper.readValue(configfile, CConfigs.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return toreturn;
    }
}
