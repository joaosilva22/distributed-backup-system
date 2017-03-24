package communications;

public class MessageConstants {
    public static final String CRLF = "\r\n";
    public static final byte CR = 0x0D;
    public static final byte LF = 0x0A;
    public static final byte SPACE = 0x20;

    public class MessageType {
        public static final String PUTCHUNK = "PUTCHUNK";
        public static final String STORED = "STORED";
        public static final String GETCHUNK = "GETCHUNK";
        public static final String CHUNK = "CHUNK";
        // TODO: Adicionar o resto dos message types
        // ...
    }
}
