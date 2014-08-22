/*
 * (c) Copyright 2009-2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.cache;

import java.io.Serializable;
import java.lang.ref.ReferenceQueue;

/**
 * Base class for cacheable data types.
 * <p>
 * It maintains a shared reference queue for housekeeping of GC'd data type
 * instances. The concrete subtypes should use this reference queue instance
 * (via the {@link #getRefQ} method) when setting up their caches.
 *
 * @author Simon Hunt
 */
public abstract class CacheableDataType implements Serializable {

    private static final long serialVersionUID = 8960662096333601281L;

    /** Our reference queue, for clean up notifications from the GC. */
    private static volatile ReferenceQueue<CacheableDataType> refQ;
    private static volatile CacheCleaner rqcc;
    private static volatile boolean ceased = false;

    /**
     * Provides subclasses with access to our reference queue. Uses lazy
     * initialization to make sure we only create a cleaner (daemon thread) if
     * we really need one.
     *
     * @return the shared reference queue
     */
    // synchronization guards access to refQ
    protected static synchronized ReferenceQueue<CacheableDataType> getRefQ() {
        if (!ceased && refQ == null) {
            refQ = new ReferenceQueue<>();
            rqcc = new CacheCleaner(refQ, "CacheableDataType");
            rqcc.start();
        }
        return refQ;
    }

    /**
     * Issues a soft cease request to the backing cache cleaner instance.
     * This should ONLY be called if you are attempting to shut down the
     * JVM, as <em>all</em> descendants of {@code CacheableDataType} use
     * this single thread for their self-trimming cache cleanup!!
     */
    static synchronized void cease() {
        ceased = true;
        if (rqcc != null) {
            rqcc.cease();
            rqcc = null;
        }
    }

    // Before we added this delimiter we had the issue where appending
    // byte values 0x0b and 0x01 would produce "11" + "1" and the converse
    // 0x01 and 0x0b producing "1" + "11" resulting in duplicate keys.
    private static final String DELIM = "/";

    /** Creates a unique string key for the given byte array.
     *
     * @param bytes the bytes
     * @return the unique key
     */
    protected static String keyFromBytes(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes)
            sb.append(b).append(DELIM);
        return sb.toString();
    }

}
