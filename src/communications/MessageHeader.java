package communications;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class MessageHeader {
    private String messageType = null, fileId = null;
    private Float version = null;
    private Integer senderId = null, chunkNo = null, replicationDeg = null;

    public MessageHeader() {}

    public MessageHeader(byte[] data) {
        ArrayList<String> fields = decodeFields(data);
        messageType = fields.get(0);
        
        switch (messageType) {
            case MessageConstants.MessageType.PUTCHUNK:
                version = Float.parseFloat(fields.get(1));
                senderId = Integer.parseInt(fields.get(2));
                fileId = fields.get(3);
                chunkNo = Integer.parseInt(fields.get(4));
                replicationDeg = Integer.parseInt(fields.get(5));
                break;
            case MessageConstants.MessageType.STORED:
                version = Float.parseFloat(fields.get(1));
                senderId = Integer.parseInt(fields.get(2));
                fileId = fields.get(3);
                chunkNo = Integer.parseInt(fields.get(4));
                break;
            // TODO: Fazer parse do resto das mensagens
            //       Fazer throw de exception quando a mesage type nao
            //       for algum dos tipos conhecidos
        }
    }

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

        builder.append(MessageConstants.CRLF);
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private ArrayList<String> decodeFields(byte[] data) {
        ArrayList<String> fields = new ArrayList<>();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (byte b : data) {
            if (b == MessageConstants.SPACE || b == MessageConstants.CR || b == MessageConstants.LF) {
                if (out.size() != 0) {
                    // TODO: Nao sei se este encoding vai funcionar para
                    //       todos os caracteres, tenho de ver
                    //       Talvez usar ASCII ??
                    String field = new String(out.toByteArray(), StandardCharsets.UTF_8);
                    fields.add(field);
                    out.reset();
                }
            } else {
                out.write(b);
            }
        }
        return fields;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getFileId() {
        return fileId;
    }

    public Float getVersion() {
        return version;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getChunkNo() {
        return chunkNo;
    }

    public int getReplicationDeg() {
        return replicationDeg;
    }
}
