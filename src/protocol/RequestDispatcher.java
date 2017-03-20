package protocol;

import communication.Message;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RequestDispatcher extends Thread {
    private ConcurrentLinkedQueue<Message> queue;
    
    public RequestDispatcher(ConcurrentLinkedQueue<Message> queue) {
        this.queue = queue;
    }

    public void run() {
        // TODO: Nao pode ser um loop infinito
        while (true) {
            if (!queue.isEmpty()) {
                Message message = queue.poll();
                if (message != null) {
                    // TODO: Apagar isto e encaminhar as mensagens para o
                    //       subprotocolo correcto
                    System.out.println("RECEIVED: " + message.getMessageType());
                    for (byte b : message.getBytes()) {
                        System.out.printf("0x%02X ", b);
                    }
                    System.out.println();
                }
            }
        }
    }
}
