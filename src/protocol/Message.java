package protocol;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class Message {
    private ArrayList<MessageHeader> headers = new ArrayList<>();
    private MessageBody body;

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
