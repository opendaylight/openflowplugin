/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.text.MessageFormat;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

import static org.opendaylight.util.StringUtils.EOL;

/**
 * Formats the JDK log message into the following form:
 * <pre>2010-04-30 12:52:36:363 PDT INFO [main:1 TestLogFormatter.testPlainString] This is a test msg.</pre>
 * The formatter catches all formatting exceptions and converts the exception to
 * a log message dumping the exception string, format string, and arguments.
 */
public class LogFormatter extends Formatter {

    // Example: 2010-01-28 16:23:48:799 PST INFO [main.1] TestExtendedLogger.basicTest This is the log message.
    static final String HDR_FMT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS:%1$tL %1$tZ %2$s [%3$s:%4$d %5$s.%6$s] ";
    
    static final int FMT_BUFF_SIZE = 255;    
    
    private StringBuffer fmtBuff;       // using StringBuffer on purpose for MessageFormat.format
    private Date date;                  // reused to format the log record date
    private String headerFmt;           // stores the header format string (so derived classes can override this)
    private boolean isHeaderLineSep;    // if true, put a line separator after the header
    
    
    /**
     * Format the provided string so that it can be logged without generating an
     * IllegalArgumentException as a result of its inherent curly braces.
     * Replaces curly braces ("{}") with square brackets ("[]"). 
     * 
     * @param toFormat String to be formatted
     * @return formatted result 
     */
    public static String prepareCurlyBraceStringForLogging(String toFormat) {
        return toFormat.trim().replaceAll("\\{", "[").replaceAll("\\}", "]");
    }
    
    
    /**
     * We can't use class.getSimpleName() because the record has the class name not the class.
     * @param clsName full class name
     * @return the class name without the package prefix.
     */
    private static String getSimpleClassName(String clsName) {
        if (null == clsName)
            return "";
        int lastDotIndex = clsName.lastIndexOf('.');
        if (lastDotIndex < 0)
            return clsName;
        return clsName.substring(lastDotIndex + 1);
    }

    /**
     * Default constructor.
     */
    public LogFormatter() {this(HDR_FMT, FMT_BUFF_SIZE, false);}
    
    /**
     * Constructor that does the real work.
     * @param headerFmt header format string
     * @param fmtBuffSize string buffer size (multiple line formatters will want to increase this)
     * @param isHeaderLineSep if true, a line separator will be appended after the header
     */
    protected LogFormatter(String headerFmt, int fmtBuffSize, boolean isHeaderLineSep) {
        this.headerFmt = headerFmt;
        this.fmtBuff = new StringBuffer(fmtBuffSize);
        this.isHeaderLineSep = isHeaderLineSep;
        this.date = new Date();
    }      
    
    /**
     * This is called by JDK logging for format the log record.  The method is synchronized to follow the pattern
     * of the {@link java.util.logging.SimpleFormatter} which allows the sharing of the buffer.
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    @Override
    public synchronized String format(LogRecord record) {

        fmtBuff.setLength(0); // reuse the internal buffer array
        
        // This wraps the formatter around the StringBuffer so the format calls will write to it.
        java.util.Formatter utilFormatter = new java.util.Formatter(fmtBuff);

        date.setTime(record.getMillis());

        Thread currThread = Thread.currentThread();
        
        // Use the utility formatter wrapping the buff to write the header.
        utilFormatter.format(headerFmt, date, record.getLevel().toString(),
                currThread.getName(), currThread.getId(),
                getSimpleClassName(record.getSourceClassName()), record.getSourceMethodName());

        if (isHeaderLineSep)
            fmtBuff.append(EOL);

        Object[] params = record.getParameters();
        String resBundleName = record.getResourceBundleName();
        
        // Check if we need to process this log message using a resource bundle.
        try {
            ResourceBundle bundle = record.getResourceBundle();
            
            if ((null == bundle) && (null != resBundleName))
                bundle = ResourceBundle.getBundle(resBundleName);

            String msgFmt = (null != bundle) ? bundle.getString(record.getMessage()) : record.getMessage();
            
            if (null != msgFmt) {
                /*
                 * This is a JDK style format use the MessageFormat class to do the substitution.
                 * We go directly to the buffer not using the utility formatter wrapping the buff.
                 */
                MessageFormat textMsgFmt = new MessageFormat(msgFmt);
                textMsgFmt.format(params, fmtBuff, null);
            }
        }
        catch (MissingResourceException e) {
            // We will get here when having a problem with the resource bundle lookup.
            fmtBuff.append('"').append(record.getMessage()).append("\" not found");
            if (null != resBundleName)
                fmtBuff.append(" in \"").append(resBundleName).append('"');
            else
                fmtBuff.append('.');
            if (null != params) {
                fmtBuff.append(" params [");
                for (int p=0; p<params.length; ++p) {
                    if (p>0) fmtBuff.append(", [");
                    fmtBuff.append(p).append("]=").append(params[p].toString());
                }
            }
        }
        catch (Exception e) {
            // We will likely get here if there is a IllegialArgumentException during parameter substitution.
            fmtBuff.append(" *** Formatting failed *** (\"").append(record.getMessage()).append('"');
            if (null != params) {
                for (Object p : params)
                    fmtBuff.append(", \"").append(p.toString()).append("\"");
            }
            fmtBuff.append(") ").append(e.toString());
        }

        if (null != record.getThrown())
            appendThrowable(record.getThrown());
        
        fmtBuff.append(EOL);

        return fmtBuff.toString();
    }    

    /**
     * Appends the stack trace of a {@link Throwable} to the buffer.  This call should only be made from within
     * the format method after the buffer has been initialize.
     * @param thr the exception to format. 
     */
    private void appendThrowable(Throwable thr) {
        fmtBuff.append(' ');
        fmtBuff.append(thr);
        StackTraceElement[] at = thr.getStackTrace();
        for (StackTraceElement elem : at) {
            fmtBuff.append(EOL);
            fmtBuff.append("\tat ");
            fmtBuff.append(elem);
        }
        formatCause(thr.getCause());
    }    

    /**
     * Recursively appends the nested {@link Throwable} which is the cause of an outer {@link Throwable} when
     * exception chaining is used. 
     * @param nestedThr a nested (a.k.a. chained) exception.
     */
    private void formatCause(Throwable nestedThr) {
        if (null == nestedThr) return;
        StackTraceElement[] at = nestedThr.getStackTrace();
        fmtBuff.append(EOL);
        fmtBuff.append("Caused by: ").append(nestedThr).append(EOL);
        fmtBuff.append("\tat ").append(at[0]).append(EOL); 
        fmtBuff.append("\t... ").append(at.length - 1).append(" more");
        formatCause(nestedThr.getCause());
    }
}
