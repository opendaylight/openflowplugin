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
 * Cleans up zombie references from {@link WeakValueCache} instances.
 *
 * @author Simon Hunt
 */
public final class CacheCleaner extends Thread {

    private ReferenceQueue<?> refQ;
    private volatile boolean stopped = false;

    /**
     * Constructs a cache cleaner for the given reference queue. Note that the
     * reference queue must only contain {@link CachedValueWeakReference}
     * instances or a class cast exception will be thrown when the cleaner
     * tries to process items on the queue. Note that type wildcard (
     * {@code ? extends Object}) refers to the <u>type of the referent</u> of
     * the weak reference, not the type of the weak reference.
     * <p>
     * This thread is marked as a daemon, to allow the JVM to shutdown
     * appropriately.
     *
     * @param refQ the reference queue to process.
     * @param namePostfix thread name postfix
     */
    public CacheCleaner(final ReferenceQueue<?> refQ, String namePostfix) {
        super("CacheCleaner-" + namePostfix);
        this.refQ = refQ;
        setDaemon(true); // make sure we don't prevent the JVM from exiting
    }


    /**
     * The run method removes (zombie) references from the queue and purges
     * the corresponding map entry from the cache's internal map. When there
     * is nothing on the queue, the thread blocks and waits.
     */
    @Override
    public void run() {
        while (!stopped) {
            try {
                CachedValueWeakReference<?, ?> ref =
                    (CachedValueWeakReference<?, ?>) refQ.remove(); // BLOCKS
                ref.purgeSelf();
            } catch (InterruptedException e) {
            }
        }
    }

    /**
     * Issues a soft cease request for the thread to stop.
     * <p>
     * This should be invoked when the system is being unloaded.
     */
    public void cease() {
        stopped = true;
        interrupt();
    }

}
