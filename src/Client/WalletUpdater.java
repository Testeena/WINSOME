package Client;

import Server.Colors;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class WalletUpdater implements Runnable{
    private MulticastSocket socket;
    private InetAddress address;
    private int port;
    private Boolean keepWorking;
    private float lastreward;

    public WalletUpdater(String address, int port){
        try {
            this.address = InetAddress.getByName(address);
            this.socket = new MulticastSocket(port);
        } catch (UnknownHostException e){
            System.err.println(Colors.RED + "Unknown Host" + Colors.RESET);
        } catch (IOException e) {
            System.err.println(Colors.RED + "Socket Error" + Colors.RESET);
        }
        this.port = port;
        this.keepWorking = true;
        this.lastreward = 0;
    }

    @Override
    public void run() {
        try{
            InetSocketAddress mcastgroup = new InetSocketAddress(address, port);
            NetworkInterface netinterface = NetworkInterface.getByInetAddress(address);
            socket.joinGroup(mcastgroup, netinterface);
            while(keepWorking){
                try{
                    ByteBuffer lenghtbytes = ByteBuffer.allocate(Integer.BYTES);
                    DatagramPacket lenghtpacket = new DatagramPacket(lenghtbytes.array(), lenghtbytes.limit());
                    socket.receive(lenghtpacket);
                    int lenght = ByteBuffer.wrap(lenghtpacket.getData()).getInt();

                    ByteBuffer to_receiveBuffer = ByteBuffer.allocate(lenght);
                    DatagramPacket rewardpacket = new DatagramPacket(to_receiveBuffer.array(), to_receiveBuffer.limit());
                    socket.receive(rewardpacket);

                    String received = new String(rewardpacket.getData(), rewardpacket.getOffset(), rewardpacket.getLength());
                    float new_rewards = Float.parseFloat(received.substring(received.indexOf(":")+1));

                    if(new_rewards != lastreward) {
                        System.out.println("\n<" + received);
                        System.out.print("> ");
                        lastreward = new_rewards;
                    }
                } catch (IOException | NumberFormatException ignored) {
                }
            }
        } catch (SocketException e) {
            System.err.println(Colors.RED + "Network Interface Error" + Colors.RESET);
        } catch (IOException e) {
            System.err.println(Colors.RED + "Join Error" + Colors.RESET);
        }
    }

    public void stopWorking(){
        this.keepWorking = false;
        socket.close();
    }
}
