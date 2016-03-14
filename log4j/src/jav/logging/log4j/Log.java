/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.logging.log4j;

import java.io.File;
import java.io.IOException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author flo
 */
public class Log {

    private static final String LOG_FORMAT = "[%p] %d{ISO8601} - %m%n";
    private static final int LOG_FILE_MAX_SIZE = 104857600;
    private static Logger logger = null;

    private Log() {
    }

    public static synchronized void setup(File basedir) {
        if (logger == null) {
            init(basedir);
        }
    }

    public static synchronized void setLevel(Level level) {
        if (logger != null) {
            logger.setLevel(level);
        }
    }

    public static Logger getLogger() {
        if (logger == null) {
            init(new File(""));
        }
        return logger;
    }

    public static void log(Object o, Level level, String fmt, Object... objs) {
        if (logger != null) {
            logger.log(level, formatMessage(o, fmt, objs));
        }
    }

    public static void info(Object o, String fmt, Object... objs) {
        log(o, Level.INFO, String.format(fmt, objs));
    }

    public static void error(Object o, String fmt, Object... objs) {
        log(o, Level.ERROR, fmt, objs);
    }

    public static void error(Object o, Exception e) {
        getLogger().error("Exception:", e);
    }

    public static void debug(Object o, String fmt, Object... objs) {
        log(o, Level.DEBUG, fmt, objs);
    }

    public static void fatal(Object o, String fmt, Object... objs) {
        log(o, Level.FATAL, fmt, objs);
    }

    private static String formatMessage(Object o, String fmt, Object... objs) {
        return String.format(
                "[%s] %s",
                o.getClass().getName(),
                String.format(fmt, objs)
        );
    }

    private static void init(File basedir) {
        try {
            logger = Logger.getLogger(Log.class);
            RollingFileAppender fileAppender = new RollingFileAppender(
                    new PatternLayout(LOG_FORMAT),
                    new File(basedir, "pocoto.log").getCanonicalPath()
            );
            fileAppender.setMaxBackupIndex(0); // no backup files
            fileAppender.setMaxBackupIndex(LOG_FILE_MAX_SIZE); // maximum log file size
            logger.addAppender(fileAppender);
            logger.setLevel(Level.ALL);
        } catch (IOException ex) {
            logger.error("Cannot access log file: ", ex);
        }
    }
}
