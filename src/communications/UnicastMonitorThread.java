package communications;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.SocketException;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UnicastMonitorThread extends Thread {
    private ConcurrentLinkedQueue<Message> queue;
    private DatagramSocket socket;

    public UnicastMonitorThread(int port, ConcurrentLinkedQueue<Message> queue) throws SocketException {
        this.queue = queue;
        socket = new DatagramSocket(port);
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
