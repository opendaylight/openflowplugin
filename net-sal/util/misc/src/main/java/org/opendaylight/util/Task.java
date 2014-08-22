/*
 * (c) Copyright 2009 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util;

/**
 * Thread capable of reporting high-level processing status and responding to
 * a cease request.
 * 
 * @author Thomas Vachuska
 */
public abstract class Task extends Thread {

    /** Flag indicating that cease stop has been requested.  */
    private volatile boolean stopped;

    /**
     * Create a new task with the specified name.
     * 
     * @param name name of the task
     */
    protected Task(String name) {
        super(name);
        setDaemon(false);
    }

    /**
     * Request peaceful shutdown of the task.
     */
    public void cease() {
        stopped = true;
    }
    
    
    /**
     * Indicates whether the task has been stopped.
     * 
     * @return true if stopped
     */
    protected boolean stopped() {
        return stopped;
    }

    
    /**
     * Provides an indication whether the task is currently busy or not.
     * 
     * @return true if the task is idle (not busy); false otherwise; the
     *         default implementation is always true
     */
    public boolean isIdle() {
        return true;
    }

    
    /**
     * Utility method to pause the current thread.
     * 
     * @param ms number of milliseconds to pause
     */
    public static void delay(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        }
    }

}
