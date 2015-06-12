/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jav.logging.log4j;

import java.io.IOException;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author flo
 */
public class Log {
    private static final String LOG_FORMAT = "[%p] %d{ISO8601} - %m%n";
    private static Logger logger = null;
    private Log() {}
    public static synchronized void setup() {
        if (logger == null) 
            init();
    }
    public static synchronized void setLevel(Level level) {
        if (logger != null) 
            logger.setLevel(level);
    }
    public static Logger getLogger() {
        if (logger == null)
            init();
        return logger;
    }
    public static void log(Object o, Level level, String fmt, Object...objs) {
        if (logger != null) 
            logger.log(level, formatMessage(o, fmt, objs));
    }
    public static void info(Object o, String fmt, Object...objs) {
        log(o, Level.INFO, String.format(fmt, objs));
    }
    public static void error(Object o, String fmt, Object...objs) {
        log(o, Level.ERROR, fmt, objs);
    }
    public static void debug(Object o, String fmt, Object...objs) {
        log(o, Level.DEBUG, fmt, objs);
    }
    public static void fatal(Object o, String fmt, Object...objs) {
        log(o, Level.FATAL, fmt, objs);
    }
    private static String formatMessage(Object o, String fmt, Object...objs) {
        return String.format(
                "[%s]\t%s", 
                o.getClass().getName(),
                String.format(fmt, objs)
        );
    }
    private static void init() {
        try {
            logger = Logger.getLogger(Log.class);
            FileAppender fileAppender = new FileAppender(
                    new PatternLayout(LOG_FORMAT), 
                    "pocoto.log"
            );
            logger.addAppender(fileAppender);
            logger.setLevel(Level.ALL);
        } catch (IOException ex) {
            logger.error("Cannot access log file: ", ex);
        }
    }
}
