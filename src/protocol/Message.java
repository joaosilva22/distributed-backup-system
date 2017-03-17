package protocol;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.ArrayList;

public class Message {
    private ArrayList<MessageHeader> headers = new ArrayList<>();
    private MessageBody body;

    public Message(byte[] data) {        
        ByteArrayOutputStream header = new ByteArrayOutputStream();
        for (int i = 0; i < data.length; i++) {
            // TODO: Tem de haver uma maneira melhor de fazer isto...
            //       Talvez rodear isto com um try catch so para evitar
            //       acessos a indices fora da array (ou dar throw e lidar
            //       com o problema noutro lado qualquer ;-D)
            if (data[i] == MessageConstants.CR && data[i + 1] == MessageConstants.LF) {
                if (data[i + 2] == MessageConstants.CR && data[i + 3] == MessageConstants.LF) {
                    byte[] bodyBytes = Arrays.copyOfRange(data, i + 4, data.length);
                    body = new MessageBody().setContent(bodyBytes);
                } else {
                    header.write(data[i]);
                    header.write(data[i+1]);
                    i += 1;
                    headers.add(new MessageHeader(header.toByteArray()));
                    header.reset();
                }
            } else {
                header.write(data[i]);
            }
        }
    }

    public Message addHeader(MessageHeader header) {
        headers.add(header);
        return this;
    }

    public Message setBody(MessageBody body) {
        this.body = body;
        return this;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            for (MessageHeader header : headers) {
                out.write(header.getBytes());
            }
            out.write(MessageConstants.CRLF.getBytes());
            out.write(body.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return out.toByteArray();
    }
}
