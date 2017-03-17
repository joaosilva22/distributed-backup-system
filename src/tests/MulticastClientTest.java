package tests;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

public class MulticastClientTest {


    public static void main(String[] args) throws UnknownHostException, InterruptedException {
        if(args.length != 2){
            System.out.println("Usage: MulticastClientTest address port");
            return;
        }

        InetAddress address = InetAddress.getByName(args[0]);
        int port = Integer.parseInt(args[1]);
        String msg = "PUTCHUNK 1 1 1 1 1 \r\n\r\n testando o multicast com esta mensagem :^)";
        byte[] buf = msg.getBytes(StandardCharsets.UTF_8);
        try( MulticastSocket clientSocket = new MulticastSocket(port) ) {
            clientSocket.joinGroup(address);
            DatagramPacket clientPacket = new DatagramPacket(buf, buf.length, address, port);
            clientSocket.send(clientPacket);
        } catch (IOException e) {

        }
    }
}
