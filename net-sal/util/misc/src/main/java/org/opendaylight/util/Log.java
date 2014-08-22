/*
 * (c) Copyright 2010-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Server-side logging. All log names should be captured in this class.
 * <p>
 * Sample usage:
 * <pre>
 * private final Logger log = Log.GENERAL.getLogger();
 * </pre>
 *
 * @author Frank Wood
 * @author Simon Hunt
 */
public enum Log {

    /**
     * Root of all loggers.
     */
    ROOT(null, "hp"),

    /**
     * General category.
     */
    GENERAL(ROOT, "general"),

    /**
     * Bootstrap category.
     */
    BOOTSTRAP(ROOT, "bootstrap"),

    /**
     * Network Buffered I/O.
     */
    NBIO(ROOT, "nbio"),

    /**
     * REST API category.
     */
    RS(ROOT, "rs"),

    /**
     * GUI category.
     */
    GUI(ROOT, "gui"),

    /**
     * Network Traffic Generator.
     */
    NTG(ROOT, "ntg"),

    /**
     * CID category.
     */
    CID(ROOT, "cid"),

    /**
     * EJB Provider category.
     */
    EJB_PROVIDER(ROOT, "ejbprov"),

    /**
     * SNMP category.
     */
    SNMP(ROOT, "snmp"),

    /**
     * Keystone service.
     */
    KEYSTONE(ROOT, "keystone"),

    /**
     * Umbrella for Cassandra services.
     */
    CASS(ROOT, "cass"),

    /**
     * Cassandra connection checker.
     */
    CASS_CONN(CASS, "conn"),

    /**
     * Umbrella for Cassandra Write Ahead Log.
     */
    CASS_WAL(CASS, "wal"),

    /**
     * Cassandra object serialization/deserialization.
     */
    CASS_WAL_OSD(CASS_WAL, "osd"),

    /**
     * Cassandra WAL group operations.
     */
    CASS_WAL_GROUP(CASS_WAL, "grp"),

    /**
     * Cassandra Write Ahead Log.
     */
    CASS_WAL_LOG(CASS_WAL, "log"),

    /**
     * Cassandra Write Ahead Log Replay.
     */
    CASS_WAL_REPLAY(CASS_WAL, "replay"),

    // required semi-colon...
    ;

    private static final String DOT = ".";

    private final String name;

    /** Constructs the log constant.
     *
     * @param parent the parent constant
     * @param name the constant (leaf) name
     */
    private Log(Log parent, String name) {
        this.name = (null == parent) ? name : parent.name + DOT + name;
    }

    /**
     * Returns the logger associated with this enumeration constant.
     *
     * @return the associated logger
     */
    public Logger getLogger() {
        return LoggerFactory.getLogger(name);
    }

    /**
     * Returns the Logger path name.
     * <p>
     * Do not use this for creating Loggers; use {@link #getLogger()} instead.
     *
     * @return the logger path name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns a multi-line string consisting of the toString() of the given
     * throwable, followed by the top {@link #SNIPPET_DEPTH} frames of the
     * stack trace for the given throwable. To be used when logging exceptions.
     * <p>
     * If the throwable has a cause defined, that too will have the top few
     * stack trace frames added to the output.
     * <p>
     * See {@link #stackTraceSnippet(Throwable, int)} for an example of usage.
     *
     * @param t the throwable
     * @return a string containing stack trace snippet(s)
     */
    public static String stackTraceSnippet(Throwable t) {
        return stackTraceSnippet(t, SNIPPET_DEPTH);
    }

    /**
     * Returns a multi-line string consisting of the toString() of the given
     * throwable, followed by the top {@code maxDepth} frames of the stack
     * trace for the given throwable. To be used when logging exceptions.
     * <p>
     * An example of how this might be used:
     * <pre>
     * private static final String MSG_BAD_CALLBACK =
     *      "Broken code? {} caused {}";
     * ...
     * private void event(Event ev) {
     *     for (EventListener el: listeners) {
     *         try {
     *             el.event(ev);
     *         } catch (Exception e) {
     *             String who = el.getClass().getName();
     *             log.warn(MSG_BAD_CALLBACK, who, Log.stackTraceSnippet(e, 3));
     *         }
     *     }
     * }
     * </pre>
     * This will result in a log message that might look something like this:
     * <pre>
     * Aug 26, 2013 12:55:10 PM org.opendaylight.util.LogTest snippet
     * WARNING: Broken code? org.opendaylight.foo.FooClass caused java.lang.RuntimeException: Test Exception
     *   org.opendaylight.foo.FooClass.handleStuff(FooClass.java:65)
     *   org.opendaylight.foo.FooClass.doEvent(FooClass.java:52)
     *   org.opendaylight.bar.EventHandler.handleEvent(EventHandler.java:137)
     *   ...
     * </pre>
     * If the throwable has a cause defined, that too will have the top few
     * stack trace frames added to the output.
     *
     * @param t the throwable
     * @param maxDepth the maximum number of stack frames to include
     * @return a string containing stack trace snippet(s)
     */
    public static String stackTraceSnippet(Throwable t, int maxDepth) {
        StringBuilder sb = new StringBuilder();
        Set<Throwable> seen = new HashSet<Throwable>();
        seen.add(t);
        Throwable current = t;
        while (current != null) {
            sb.append(current);
            StackTraceElement[] stack = current.getStackTrace();
            int n = 0;
            while (n < maxDepth && n < stack.length)
                sb.append(EOLI).append(stack[n++]);
            if (n == maxDepth)
                sb.append(ELLIPSIS);
            // recurse on cause
            current = current.getCause();
            if (current != null) {
                sb.append(CAUSED_BY);

                // prevent infinite loops
                if (seen.contains(current))
                    current = null;
                else
                    seen.add(current);
            }
        }

        return sb.toString();
    }

    private static final int SNIPPET_DEPTH = 5;
    private static final String EOLI = StringUtils.EOL + "  ";
    private static final String ELLIPSIS = EOLI + "...";
    private static final String CAUSED_BY = StringUtils.EOL + "Caused by: ";
}
