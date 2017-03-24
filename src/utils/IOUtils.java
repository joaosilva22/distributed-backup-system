package utils;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class IOUtils {
    private static String logFileName = null;
    
    private IOUtils() {}

    public static String getLogFileName() {
        if (logFileName == null) {
            // TODO: Utilizar uma constante em vez de ./logs/
            new File("./logs/").mkdir();
            logFileName = "./logs/" + getCurrentTimeStamp() + ".log";
            File logFile = new File(logFileName);
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return logFileName;
    }
    
    public static String getCurrentTimeStamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    public static void log(String message) {
        String timestampedMessage = "[" + getCurrentTimeStamp() + "] " + message;
        System.out.println(timestampedMessage);

        String log = IOUtils.getLogFileName();
        if (log != null) {
            try(PrintWriter out = new PrintWriter(new FileOutputStream(log, true))) {
                out.println(timestampedMessage);
            } catch (FileNotFoundException e) {
                // TODO: Alguma coisa?
            }
        }
    }

    public static void warn(String message) {
        String timestampedMessage = "[" + getCurrentTimeStamp() + "] " + message;
        System.out.println((char)27 + "[33m" + timestampedMessage + (char)27 + "[0m");

        String log = IOUtils.getLogFileName();
        if (log != null) {
            try(PrintWriter out = new PrintWriter(new FileOutputStream(log, true))) {
                out.println(timestampedMessage);
            } catch (FileNotFoundException e) {
                // TODO: Alguma coisa?
            }
        }
    }

    public static void err(String message) {
        String timestampedMessage = "[" + getCurrentTimeStamp() + "] " + message;
        System.out.println((char)27 + "[31m" + timestampedMessage + (char)27 + "[0m");

        String log = IOUtils.getLogFileName();
        if (log != null) {
            try(PrintWriter out = new PrintWriter(new FileOutputStream(log, true))) {
                out.println(timestampedMessage);
            } catch (FileNotFoundException e) {
                // TODO: Alguma coisa?
            }
        }
    }
}
