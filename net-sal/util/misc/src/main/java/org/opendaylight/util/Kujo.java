/*
 * (c) Copyright 2011 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Utility to hunt down threads by their name and/or terminate the rogue
 * ones.
 * 
 * @author Thomas Vachuska
 */
public class Kujo {

    private static ThreadGroup rootGroup = null;
    
    /** Prevent construction. */
    private Kujo() {}
    
    /**
     * Interrupts any threads whose name matches the specified regex.
     *  
     * @param regex regular expression identifying threads to be interrupted
     */
    public static void bite(String regex) {
        for (Thread t : getAllThreads()) {
            if (t.getName().matches(regex))
                t.interrupt();
        }
    }
    
    /**
     * Stops any threads whose name matches the specified regex.
     *  
     * @param regex regular expression identifying threads to be stopped
     */
    @SuppressWarnings("deprecation")
    public static void kill(String regex) {
        for (Thread t : getAllThreads()) {
            if (t.getName().matches(regex))
                t.stop();
        }
    }
    
    /**
     * Get all threads in the VM.
     * 
     * @return array of all threads
     */
    private static Thread[] getAllThreads() {
        ThreadGroup root = getRootThreadGroup();
        ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
        int nAlloc = thbean.getThreadCount();
        int n = 0;
        Thread[] threads;
        do {
            nAlloc *= 2;
            threads = new Thread[nAlloc];
            n = root.enumerate(threads, true);
        } while (n == nAlloc);
        return java.util.Arrays.copyOf(threads, n);
    }

    
    /**
     * Find the root thread group.
     * 
     * @return root thread group
     */
    private synchronized static ThreadGroup getRootThreadGroup() {
        if (rootGroup == null) {
            ThreadGroup pg;
            rootGroup = Thread.currentThread().getThreadGroup();
            while ((pg = rootGroup.getParent()) != null)
                rootGroup = pg;
        }
        return rootGroup;
    }
    
}
