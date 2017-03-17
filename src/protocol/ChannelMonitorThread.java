package protocol;

import java.net.MulticastSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.io.IOException;
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
        // TODO: Isto provavelmente nao devia ser um ciclo infinito
        while (true) {
            // TODO: Nao sei se este tamanho para buffers funciona
            byte[] buf = new byte[64 * 1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            try {
                socket.receive(packet);
                Message message = new Message(packet.getData());
                queue.add(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
