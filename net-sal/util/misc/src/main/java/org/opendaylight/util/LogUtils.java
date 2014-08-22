/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.util.Enumeration;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static org.opendaylight.util.StringUtils.EOL;


/**
 * Useful Logging utilities.
 * 
 * @author Frank Wood
 */
public final class LogUtils {

    static final String JDK_LOG_LEVEL_PROP_KEY = "jdk.log.level";

    /**
     * Setup the JDK root and global handlers to use our formatter and
     * sets the level for the handlers to the passed in level.
     * @param level log level
     */
    public static void configureJdkLogging(Level level) {
        Logger log = Logger.getLogger("");
        log.setLevel(level);
    
        // Create a console handler.
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new LogFormatter());
        ch.setLevel(level);
    
        // Remove the existing default handler.
        Handler[] dhs = log.getHandlers();
        for (Handler h : dhs)
            log.removeHandler(h);

        // Add our new handler with our formatter.
        log.addHandler(ch);
        log.setUseParentHandlers(false);
    }
    
    /**
     * Setup the JDK root and global handlers to use our formatter and
     * sets the level for the handlers based on the value of the
     * {@link #JDK_LOG_LEVEL_PROP_KEY} property.
     */
    public static void configureJdkLoggingFromProperty() {
        String s = System.getProperty(JDK_LOG_LEVEL_PROP_KEY);
        configureJdkLogging((null != s) ? Level.parse(s) : Level.OFF);
    }

    /**
     * Returns a multi-line string representing the current state of
     * the JDK logger (loggers, handlers and formatters).
     *
     * @return string representation of logger state
     */
    public static String getJdkLogState() {

        LogManager manager = LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();

        StringBuilder sb = new StringBuilder(1024);

        sb.append("Logger Information:").append(EOL);
        while (names.hasMoreElements()) {
            String loggername = names.nextElement();
            Logger logger = manager.getLogger(loggername);

            if (null == logger) {
                sb.append("  --------------------------------------------------------------------------------");
                sb.append(EOL);
                sb.append("  Logger name:  [").append(loggername).append("] IS NULL").append(EOL);
                continue;
            }

            sb.append("  --------------------------------------------------------------------------------").append(EOL);
            sb.append("  Logger name:  [").append(logger.getName()).append("]").append(EOL);
            sb.append("  Logger level: [").append(logger.getLevel()).append("]").append(EOL);

            if (logger.getFilter() != null)
                sb.append("  Using a filter").append(EOL);
            else
                sb.append("  No filter used").append(EOL);

            Handler[] h = logger.getHandlers();
            if (h.length == 0)
                sb.append("  No handlers defined").append(EOL);
            for (int i = 0; i < h.length; i++) {
                if (i == 0)
                    sb.append("  Handlers:").append(EOL);
                Formatter f = h[i].getFormatter();
                sb.append("    ").append(h[i].getClass().getName()).append(EOL);
                sb.append("      using formatter: [").append(f.getClass().getName()).append("]").append(EOL);
                sb.append("      using level: [").append(h[i].getLevel()).append("]").append(EOL);
                if (h[i].getFilter() != null)
                    sb.append("      using a filter").append(EOL);
                else
                    sb.append("      no filter").append(EOL);
            }

            if (logger.getUseParentHandlers())
                sb.append("  Forward to parent loggers").append(EOL);
            else
                sb.append("  Not forwarding to parent loggers").append(EOL);

            Logger parent = logger.getParent();
            if (parent != null)
                sb.append("  Parent: [").append(parent.getName()).append("]").append(EOL);
            else
                sb.append("  No parent (this is root)").append(EOL);
        }
        sb.append("  --------------------------------------------------------------------------------").append(EOL);

        return sb.toString();
    }
}
