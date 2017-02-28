package protocol;
// TODO: Um nome melhor para a package seria 'protocol'

import java.util.ArrayList;

public class MessageBuilder {
    private ArrayList<MessageHeader> headers;
    private MessageBody body;

    public MessageBuilder addHeader(MessageHeader header) {
        headers.add(header);
        return this;
    }

    public MessageBuilder setBody(MessageBody body) {
        this.body = body;
        return this;
    }

    // public byte[] build() {}
}
