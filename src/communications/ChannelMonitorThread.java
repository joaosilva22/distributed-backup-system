package communications;

import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ChannelMonitorThread extends Thread {
    private String address;
    private int port;
    private ConcurrentLinkedQueue<Message> queue;
    private MulticastSocket socket;
    
    public ChannelMonitorThread(String address, int port, ConcurrentLinkedQueue<Message> queue) throws UnknownHostException, IOException {
        this.address = address;
        this.port = port;
        this.queue = queue;

        InetAddress group = InetAddress.getByName(address);
        socket = new MulticastSocket(port);
        socket.joinGroup(group);
    }

    public void run() {
        while (true) {
            byte[] buf = new byte[64 * 1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                byte[] data = Arrays.copyOfRange(packet.getData(), 0, packet.getLength());
                Message message = new Message(data);
                message.setSenderAddress(packet.getAddress());
                queue.add(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
