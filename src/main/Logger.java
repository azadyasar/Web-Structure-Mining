package main;


import java.io.File;
import java.util.Date;

public class Logger {

    private static File logFile_;
    private static Logger logger_;

    private Logger() {
        if (logFile_ != null) {
            this.logFile_ = new File(logFile_.getPath() + File.separator + "log_" + new Date().getTime());
            System.out.printf("Logger created %s-%s\n", logFile_.getPath(), logFile_.getName());
        }
    }
    
    public static Logger getLogger() {
        if (logger_ == null)
            logger_ = new Logger();
        return logger_;
    }
    
    public static void setFolder(File folder) {
        if (logFile_ == null) {
            logFile_ = folder;
            System.out.printf("Folder set %s\n", folder.getPath());
        }
    }
    
    public void log(String info) {
        FileReaderEx.writeToFile(logFile_, info);
    }

}
