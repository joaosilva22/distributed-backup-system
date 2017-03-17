package communication;

import java.nio.charset.StandardCharsets;

public class MessageBody {
    private byte[] content = null;

    public MessageBody setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public byte[] getBytes() {
        return content;
    }
}
