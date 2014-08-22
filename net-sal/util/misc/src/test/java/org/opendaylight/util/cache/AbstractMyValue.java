/*
 * (c) Copyright 2009,2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.util.cache;

import java.lang.ref.ReferenceQueue;

/**
 * This class is the superclass of the MyValueXXX classes.
 *
 * @author Simon Hunt
 */
public abstract class AbstractMyValue {

    /** our reference queue, for clean up notifications from the GC. */
    private static ReferenceQueue<AbstractMyValue> refQ;
    private static CacheCleaner rqcc;

    /** Subclasses should use this to get a reference to the shared queue.
     *
     * @return the reference queue
     */
    // synchronization guards access to refQ
    protected static synchronized ReferenceQueue<AbstractMyValue> getRefQ() {
        if (refQ == null) {
            refQ = new ReferenceQueue<AbstractMyValue>();
            rqcc = new CacheCleaner(refQ, "AbstractMyValue");
            rqcc.start();
        }
        return refQ;
    }

    /**
     * Issues a soft cease request to the backing cache cleaner instance.
     */
    public static synchronized void cease() {
        if (rqcc != null) {
            rqcc.cease();
            rqcc = null;
        }
    }

}
