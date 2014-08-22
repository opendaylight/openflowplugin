/*
 * (c) Copyright 2013,2014 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.impl;

import org.opendaylight.of.lib.msg.MessageFactory;
import org.opendaylight.util.api.security.SecurityContext;
import org.opendaylight.util.net.IpAddress;

import java.util.Set;
import java.util.TreeSet;

import static java.util.Collections.unmodifiableSet;
import static org.opendaylight.util.PrimitiveUtils.verifyU16;

/**
 * Encapsulates the run-time configuration parameters for the
 * {@link OpenflowController}. Instances of this class are immutable.
 * <p>
 * The values (with defaults shown in square brackets) are:
 * <ul>
 *     <li>
 *         <strong>addresses</strong>: the set of interface addresses to
 *         listen on; {@code null} (or the empty set) denotes listening on all
 *         available interfaces. <em>[null]</em>
 *     </li>
 *     <li>
 *         <strong>listenPort</strong>: the OpenFlow listen port for
 *         non-secure connections. <em>[{@value #DEF_PORT}]</em>
 *     </li>
 *     <li>
 *         <strong>tlsListenPort</strong>: the OpenFlow listen port for
 *         secure (TLS) connections. <em>[{@value #DEF_TLS_PORT}]</em>
 *     </li>
 *     <li>
 *         <strong>udpPort</strong>: the OpenFlow port for UDP.
 *         <em>[{@value #DEF_UDP_PORT}]</em>
 *     </li>
 *     <li>
 *         <strong>securityContext</strong>: the crypt context for
 *         secure communications. <em>[null]</em>
 *     </li>
 *     <li>
 *         <strong>suppressSetConfig</strong>: the <em>SetConfig</em> behavior
 *         suppression flag. A value of {@code false} means that the controller
 *         will send a <em>SetConfig</em> message to a datapath, soon after it
 *         completes its handshake. A value of {@code true} means this behavior
 *         is suppressed (i.e. no <em>SetConfig</em> message is sent).
 *         <em>[false]</em>
 *     </li>
 *     <li>
 *         <strong>rcvBufSize</strong>: the receive buffer size for TCP or TLS
 *         connection oriented transport. <em>[{@value #DEF_RCV_BUF_SIZE}]</em>
 *     </li>
 *     <li>
 *         <strong>udpRcvBufSize</strong>: the recive buffer size for UDP
 *         connectionless transport. <em>[{@value #DEF_UDP_RCV_BUF_SIZE}]</em>
 *     </li>
 *     <li>
 *         <strong>workerCount</strong>: number of I/O workers.
 *         <em>[{@value #DEF_WORKER_COUNT}]</em>
 *     </li>
 *     <li>
 *         <strong>idleCheckMs</strong>: milliseconds between checks for idle
 *         connections. <em>[{@value #DEF_IDLE_CHECK_MS}]</em>
 *     </li>
 *     <li>
 *         <strong>maxIdleMs</strong>: milliseconds to consider connection
 *         to be idle. <em>[{@value #DEF_MAX_IDLE_MS}]</em>
 *     </li>
 *     <li>
 *         <strong>maxEchoMs</strong>: milliseconds between echo requests
 *         on idle connections. <em>[{@value #DEF_MAX_ECHO_MS}]</em>
 *     </li>
 *     <li>
 *         <strong>maxEchoAttempts</strong>: number of attempts to wake idle
 *         connections. <em>[{@value #DEF_MAX_ECHO_ATTEMPTS}]</em>
 *     </li>
 *     <li>
 *         <strong>strictMessageParsing</strong>: a value of {@code true} will
 *         cause the {@link MessageFactory} to employ strict parsing of
 *         OpenFlow messages. <em>[false]</em>
 *     </li>
 *     <li>
 *         <strong>enforcementLevel</strong>: enforcement level of flow mod
 *         compliance against prior flow mod class registrations.
 *         <em>["weak"]</em>
 *     </li>
 *     <li>
 *         <strong>hybridMode</strong>: a value of {@code true} will allow
 *         controlled switch to decide primary packet flow. <em>[true]</em>
 *     </li>
 * </ul>
 * <p>
 * This class uses the builder pattern. For example, to create an instance
 * where we supply a crypt context and also set strict message parsing
 * in the <em>OpenFlow Message Library</em>, the following code might be used:
 * <pre>
 * ControllerConfig cfg = new ControllerConfig.Builder()
 *         .securityContext(secCtx).strictMessageParsing().build();
 * </pre>
 * Note that all other parameters will have default values.
 *
 * @author Scott Simes
 * @author Simon Hunt
 * @author Sudheer Duggisetty
 * @author Thomas Vachuska
 */
public class ControllerConfig {

    /*
     * IMPLEMENTATION NOTE:
     * All boolean values should default to false, and the associated
     * setter should be a no-args method that sets the flag to true.
     */

    /** The default OpenFlow listen port (6633). */
    public static final int DEF_PORT = 6633;

    /** The default OpenFlow TLS listen port (6634). */
    public static final int DEF_TLS_PORT = 6634;

    /** The default OpenFlow UDP port (6635). */
    public static final int DEF_UDP_PORT = 6635;

    // TODO: research a good value for this (this was from some example code)
    /** The default buffer size for TCP / TLS connections. */
    public static final int DEF_RCV_BUF_SIZE = 1048576;

    /** The default buffer size for UDP connections. */
    public static final int DEF_UDP_RCV_BUF_SIZE = 1024;

    /** The default I/O loop worker count. */
    public static final int DEF_WORKER_COUNT = 16;

    /** Default number of milliseconds between idle connection checks. */
    public static final int DEF_IDLE_CHECK_MS = 500;

    /** Default number of milliseconds to consider connections idle. */
    public static final int DEF_MAX_IDLE_MS = 5000;

    /** Default number of attempts to wake idle connection with echo requests. */
    public static final int DEF_MAX_ECHO_ATTEMPTS = 5;

    /** Default number of milliseconds between idle connection echo requests. */
    public static final int DEF_MAX_ECHO_MS = 5000;

    /** Default flow mod enforcement level. */
    public static final String DEF_ENFORCEMENT_LEVEL = "weak";

    /** Default setting for hybrid mode. */
    public static final boolean DEF_HYBRID_MODE = false;

    //====
    private final Set<IpAddress> addresses;
    private final int listenPort;
    private final int tlsListenPort;
    private final int udpPort;
    private final SecurityContext securityContext;

    private final boolean suppressSetConfig;

    private final int rcvBufSize;
    private final int udpRcvBufSize;

    private final int workerCount;

    private final int idleCheckMs;
    private final int maxIdleMs;
    private final int maxEchoMs;
    private final int maxEchoAttempts;

    private final boolean strictMessageParsing;
    private final boolean hybridMode;

    /**
     * Constructs a controller configuration instance using the specified
     * parameters. This private constructor is called from Builder.build().
     *
     * @param addresses the set of interface addresses to listen on
     * @param listenPort the OpenFlow listen port
     * @param tlsListenPort the OpenFlow TLS listen port
     * @param udpPort the OpenFlow UDP listen port
     * @param securityContext the crypt context parameters
     * @param suppressSetConfig indicates whether to suppress
     *                          <em>SetConfig</em> behavior
     * @param rcvBufSize the TCP or TLS receive buffer size
     * @param udpRcvBufSize the UDP receive buffer size
     * @param workerCount number of I/O workers
     * @param idleCheckMs milliseconds between idle connection checks
     * @param maxIdleMs milliseconds to consider connection as idle
     * @param maxEchoMs milliseconds between echo request attempts
     * @param maxEchoAttempts number of echo request attempts
     * @param strictMessageParsing true to invoke strict parsing of OpenFlow
     *                             messages by the message library
     * @param hybridMode true if hybridMode is enabled
     */
    private ControllerConfig(Set<IpAddress> addresses, int listenPort,
                             int tlsListenPort, int udpPort,
                             SecurityContext securityContext,
                             boolean suppressSetConfig,
                             int rcvBufSize, int udpRcvBufSize,
                             int workerCount, int idleCheckMs,
                             int maxIdleMs, int maxEchoMs,
                             int maxEchoAttempts, boolean strictMessageParsing,
                             boolean hybridMode) {
        this.addresses = addresses;
        this.listenPort = listenPort;
        this.tlsListenPort = tlsListenPort;
        this.udpPort = udpPort;
        this.securityContext = securityContext;
        this.suppressSetConfig = suppressSetConfig;
        this.rcvBufSize = rcvBufSize;
        this.udpRcvBufSize = udpRcvBufSize;
        this.workerCount = workerCount;
        this.idleCheckMs = idleCheckMs;
        this.maxIdleMs = maxIdleMs;
        this.maxEchoMs = maxEchoMs;
        this.maxEchoAttempts = maxEchoAttempts;
        this.strictMessageParsing = strictMessageParsing;
        this.hybridMode = hybridMode;
    }

    private String addrToString() {
        return (addresses == null || addresses.size() == 0)
                ? "ALL" : addresses.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ControllerConfig: i/f=")
                .append(addrToString()).append(",port=")
                .append(listenPort).append(",tls=").append(tlsListenPort)
                .append(",udp=").append(udpPort).append(",secCtx=")
                .append(securityContext == null ? "null" : "****");

        if (suppressSetConfig)
            sb.append(",suppressSetConfig");
        if (strictMessageParsing)
            sb.append(",strictMessageParsing");
        if (hybridMode)
            sb.append(",hybridMode");

        sb.append(",rcvBufSize=").append(rcvBufSize);
        sb.append(",udpRcvBufSize=").append(udpRcvBufSize);
        sb.append(",workerCount=").append(workerCount);
        sb.append(",idleCheckMs=").append(idleCheckMs);
        sb.append(",maxIdleMs=").append(maxIdleMs);
        sb.append(",maxEchoMs=").append(maxEchoMs);
        sb.append(",maxEchoAttempts=").append(maxEchoAttempts);
        sb.append("}");
        return sb.toString();
    }

    /**
     * Returns the set of addresses to listen on.
     * May be null, or an empty set, which indicates all available interfaces.
     *
     * @return the set of addresses
     */
    public Set<IpAddress> addresses() {
        return addresses;
    }

    /**
     * Returns the OpenFlow listen port for non-secure connections.
     *
     * @return the OpenFlow listen port
     */
    public int listenPort() {
        return listenPort;
    }

    /**
     * Returns the OpenFlow listen port for secure (TLS) connections.
     *
     * @return the OpenFlow TLS listen port
     */
    public int tlsListenPort() {
        return tlsListenPort;
    }

    /**
     * Returns the OpenFlow UDP port.
     *
     * @return the OpenFlow UDP port
     */
    public int udpPort() {
        return udpPort;
    }

    /**
     * Returns the crypt context for the secure (TLS) connection.
     *
     * @return the crypt context
     */
    public SecurityContext securityContext() {
        return securityContext;
    }

    /**
     * Returns the <em>SetConfig</em> behavior suppression flag.
     * <p>
     * A value of {@code false} means that the controller will send
     * a <em>SetConfig</em> message to a datapath, soon after it completes
     * its handshake. A value of {@code true} means this behavior is
     * suppressed (i.e. no <em>SetConfig</em> message is sent).
     *
     * @return the <em>SetConfig</em> suppression flag
     */
    public boolean suppressSetConfig() {
        return suppressSetConfig;
    }

    /**
     * Returns the TCP or TLS receive buffer size.
     *
     * @return the TCP or TLS receive buffer size
     */
    public int rcvBufSize() {
        return rcvBufSize;
    }

    /**
     * Returns the UDP receive buffer size.
     *
     * @return the UDP receive buffer size
     */
    public int udpRcvBufSize() {
        return udpRcvBufSize;
    }

    /**
     * Returns the I/O loop worker count.
     *
     * @return number of I/O loop workers
     */
    public int workerCount() {
        return workerCount;
    }

    /**
     * Returns the period of idle connection detection.
     *
     * @return milliseconds between idle connection checks
     */
    public int idleCheckMs() {
        return idleCheckMs;
    }

    /**
     * Returns the milliseconds before connections are considered idle.
     *
     * @return milliseconds before connection is considered idle
     */
    public int maxIdleMs() {
        return maxIdleMs;
    }

    /**
     * Returns the period of echo requests on idle connections.
     *
     * @return milliseconds between echo requests on idle connections
     */
    public int maxEchoMs() {
        return maxEchoMs;
    }

    /**
     * Returns the number of tries to wake idle connections.
     *
     * @return number of attempts allowed to wake idle connections
     */
    public int maxEchoAttempts() {
        return maxEchoAttempts;
    }

    /**
     * Returns true if the message factory will use strict parsing of OpenFlow
     * messages. Generally speaking, this means that the parser will throw an
     * exception if 1-bits are discovered in non-spec-defined areas of bitmasks,
     * amongst other items.
     *
     * @return true if message factory is to employ strict parsing
     */
    public boolean strictMessageParsing() {
        return strictMessageParsing;
    }


    /**
     * Returns true if Hybrid Mode is enabled. Hybrid Mode allows the switch to
     * decide packet forwarding.
     *
     * @return true if hybrid mode is enabled
     */
    public boolean hybridMode() {
        return hybridMode;
    }

    /**
     * Predicate that returns true if the difference between this (newer)
     * controller configuration and the specified (older) configuration would
     * require the controller to be bounced for the configuration changes to
     * take effect.
     * <p>
     * Currently, this will return true if:
     * <ul>
     *     <li> the set of IP addresses on which to bind has changed </li>
     *     <li> any of the listen ports have changed </li>
     *     <li> the crypt context has changed </li>
     *     <li> any receive buffer sizes have changed </li>
     *     <li> the worker count has changed </li>
     *     <li> the idle check period has changed </li>
     *     <li> hybrid mode has changed </li>
     * </ul>
     *
     * @param old the old configuration to compare with
     * @return true, if the controller should be bounced
     */
    public boolean bounceRequired(ControllerConfig old) {
        return  old == null ||
                addressesDiffer(old.addresses) ||
                listenPort != old.listenPort ||
                tlsListenPort != old.tlsListenPort ||
                udpPort != old.udpPort ||
                securityContextsDiffer(old.securityContext) ||
                rcvBufSize != old.rcvBufSize ||
                udpRcvBufSize != old.udpRcvBufSize ||
                workerCount != old.workerCount ||
                idleCheckMs != old.idleCheckMs ||
                hybridMode != old.hybridMode;
    }

    /**
     * Returns true if the given addresses are not the same as our addresses.
     *
     * @param oldAddrs the old addresses
     * @return true, if the given addresses differ from our addresses
     */
    boolean addressesDiffer(Set<IpAddress> oldAddrs) {
        if (addresses == null)
            return oldAddrs != null;
        return !addresses.equals(oldAddrs);
    }

    /**
     * Returns true if the given crypt context is not the same as our
     * crypt context.
     *
     * @param oldCtx the old context
     * @return true, if the given context differs from our context
     */
    boolean securityContextsDiffer(SecurityContext oldCtx) {
        if (securityContext == null)
            return oldCtx != null;
        return !securityContext.equals(oldCtx);
    }

    //======================================================================

    /** Builds instances of ControllerConfig. */
    public static class Builder {
        /*
         * IMPLEMENTATION NOTE:
         * All boolean values should default to false, and the associated
         * setter should be a no-args method that sets the flag to true.
         */

        // explicitly declaring all default values (even nulls and false)...
        private Set<IpAddress> addresses = null;
        private int listenPort = DEF_PORT;
        private int tlsListenPort = DEF_TLS_PORT;
        private int udpPort = DEF_UDP_PORT;
        private SecurityContext securityContext = null;
        private boolean suppressSetConfig = false;
        private int rcvBufSize = DEF_RCV_BUF_SIZE;
        private int udpRcvBufSize = DEF_UDP_RCV_BUF_SIZE;
        private int workerCount = DEF_WORKER_COUNT;
        private int idleCheckMs = DEF_IDLE_CHECK_MS;
        private int maxIdleMs = DEF_MAX_IDLE_MS;
        private int maxEchoMs = DEF_MAX_ECHO_MS;
        private int maxEchoAttempts = DEF_MAX_ECHO_ATTEMPTS;
        private boolean strictMessageParsing = false;
        private boolean hybridMode = DEF_HYBRID_MODE;

        /**
         * Returns a controller configuration for the current settings on
         * this builder.
         *
         * @return the controller configuration
         */
        public ControllerConfig build() {
            return new ControllerConfig(addresses, listenPort, tlsListenPort,
                    udpPort, securityContext, suppressSetConfig, rcvBufSize,
                    udpRcvBufSize, workerCount, idleCheckMs, maxIdleMs,
                    maxEchoMs, maxEchoAttempts, strictMessageParsing,
                    hybridMode);
        }

        /**
         * Sets the addresses that the controller should listen on. A value
         * of null (or an empty set) is interpreted to mean every available
         * interface.
         *
         * @param addr the addresses to listen on
         * @return self, for chaining
         */
        public Builder addresses(Set<IpAddress> addr) {
            addresses = (addr == null || addr.isEmpty())
                    ? null : unmodifiableSet(new TreeSet<>(addr));
            return this;
        }

        /**
         * Sets the OpenFlow listen port for non-secure connections.
         *
         * @param port the port number
         * @return self, for chaining
         * @throws IllegalArgumentException if the port number is not valid
         */
        public Builder listenPort(int port) {
            verifyU16(port);
            listenPort = port;
            return this;
        }

        /**
         * Sets the OpenFlow listen port for secure (TLS) connections.
         *
         * @param port the port number
         * @return self, for chaining
         * @throws IllegalArgumentException if the port number is not valid
         */
        public Builder tlsListenPort(int port) {
            verifyU16(port);
            tlsListenPort = port;
            return this;
        }

        /**
         * Sets the OpenFlow port for UDP.
         *
         * @param port the port number
         * @return self, for chaining
         * @throws IllegalArgumentException if the port number is not valid
         */
        public Builder udpPort(int port) {
            verifyU16(port);
            udpPort = port;
            return this;
        }

        /**
         * Sets the crypt context.
         *
         * @param secCtx the crypt context
         * @return self, for chaining
         */
        public Builder securityContext(SecurityContext secCtx) {
            securityContext = secCtx;
            return this;
        }

        /**
         * Sets the <em>SetConfig</em> behavior suppression flag
         * to {@code true}.
         *
         * @return self, for chaining
         */
        public Builder suppressSetConfig() {
            suppressSetConfig = true;
            return this;
        }

        /**
         * Sets the TCP or TLS receive buffer size.
         *
         * @param size the TCP or TLS receive buffer size
         * @return self, for chaining
         */
        public Builder rcvBufSize(int size) {
            rcvBufSize = size;
            return this;
        }

        /**
         * Sets the UDP receive buffer size.
         *
         * @param size the UDP receive buffer size
         * @return self, for chaining
         */
        public Builder udpRcvBufSize(int size) {
            udpRcvBufSize = size;
            return this;
        }

        /**
         * Sets the worker count.
         *
         * @param count number of I/O workers
         * @return self, for chaining
         */
        public Builder workerCount(int count) {
            workerCount = count;
            return this;
        }

        /**
         * Sets the number of milliseconds between idle connection checks.
         *
         * @param ms idle check period in milliseconds
         * @return self, for chaining
         */
        public Builder idleCheckMs(int ms) {
            idleCheckMs = ms;
            return this;
        }

        /**
         * Sets the maximum number of milliseconds before a connection is
         * considered idle.
         *
         * @param ms idle check period in milliseconds
         * @return self, for chaining
         */
        public Builder maxIdleMs(int ms) {
            maxIdleMs = ms;
            return this;
        }

        /**
         * Sets the echo request period.
         *
         * @param ms milliseconds between echo requests
         * @return self, for chaining
         */
        public Builder maxEchoMs(int ms) {
            maxEchoMs = ms;
            return this;
        }

        /**
         * Sets the echo request attempt limit.
         *
         * @param count number of echo request attempts
         * @return self, for chaining
         */
        public Builder maxEchoAttempts(int count) {
            maxEchoAttempts = count;
            return this;
        }

        /**
         * Sets the <em>strict message parsing</em> behavior to {@code true}.
         *
         * @return self, for chaining
         */
        public Builder strictMessageParsing() {
            strictMessageParsing = true;
            return this;
        }

        /**
         * Sets the <em>Hybrid Mode</em> behavior to the specified value.
         *
         * @param value the new value for hybrid mode
         * @return self, for chaining
         */
        public Builder hybridMode(boolean value) {
            hybridMode = value;
            return this;
        }
    }

}
