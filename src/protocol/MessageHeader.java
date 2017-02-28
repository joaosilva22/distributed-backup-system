package protocol;

import java.nio.charset.StandardCharsets;

public class MessageHeader {
    // TODO: Mudar isto para uma classe de constantes
    private static final String CRLF = "\r\n";
    
    private String messageType = null, fileId = null;
    private Float version = null;
    private Integer senderId = null, chunkNo = null, replicationDeg = null;

    public MessageHeader setMessageType(String messageType) {
        this.messageType = messageType;
        return this;
    }

    public MessageHeader setFileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public MessageHeader setVersion(float version) {
        this.version = version;
        return this;
    }

    public MessageHeader setSenderId(int senderId) {
        this.senderId = senderId;
        return this;
    }

    public MessageHeader setChunkNo(int chunkNo) {
        this.chunkNo = chunkNo;
        return this;
    }

    public MessageHeader setReplicationDeg(int replicationDeg) {
        this.replicationDeg = replicationDeg;
        return this;
    }

    public byte[] getBytes() {
        StringBuilder builder = new StringBuilder();

        if (messageType != null) {
            builder.append(messageType);
            builder.append(" ");
        }

        if (fileId != null) {
            builder.append(fileId);
            builder.append(" ");
        }

        if (version != null) {
            builder.append(version.toString());
            builder.append(" ");
        }

        if (senderId != null) {
            builder.append(senderId.toString());
            builder.append(" ");
        }

        if (chunkNo != null) {
            builder.append(chunkNo.toString());
            builder.append(" ");
        }

        if (replicationDeg != null) {
            builder.append(replicationDeg.toString());
            builder.append(" ");
        }

        builder.append(CRLF);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }
}
