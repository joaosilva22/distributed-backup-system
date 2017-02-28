package protocol;

import java.nio.charset.StandardCharsets;

public class MessageBody {
    private String content = null;

    public MessageBody setContent(String content) {
        this.content = content;
        return this;
    }

    public byte[] getBytes() {
        return content.getBytes(StandardCharsets.UTF_8);
    }
}
