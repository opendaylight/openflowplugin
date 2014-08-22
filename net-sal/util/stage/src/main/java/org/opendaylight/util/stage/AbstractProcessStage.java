/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Base implementation of a {@link ProcessStageOutlet process stage outlet}
 * semantics.
 * 
 * @author Thomas Vachuska
 * @author Frank Wood
 * @author John Green
 * @author Simon Hunt
 * 
 * @param <T> type of item accepted/taken by the stage 
 * @param <P> type of item produced by the stage
 * @param <B> type of alternate item produced by the stage when branching
 */
public abstract class AbstractProcessStage<T, P, B> 
    implements ProcessStageOutlet<T, P>, BranchingStackableOutlet<T, P, B> {

    /**
     * Default number of milliseconds for the {@link #setIdleTimeOut(long)
     * idle timeout} setting.
     */ 
    public static final long DEFAULT_IDLE_MILLIS = 15000;

    // Downstream, discard and branch outlets
    private Outlet<P> downstreamOutlet = null;
    private Outlet<T> discardOutlet = null;
    private Outlet<B> branchOutlet = null;

    // Indicator for tracking whether we're started or stopped.
    private volatile boolean started = false;

    // Tally of items that have been accepted, but not yet output
    private volatile int tally = 0;

    // Time-stamp of the last time an item was accepted or output.
    private long lastTallyTime;

    // Current time-out since last activity, before returning to idle state.
    private long idleTimeOut = DEFAULT_IDLE_MILLIS;

    @Override
    public boolean accept(T item) {
        // If we have not been started, decline the item.
        if (!started)
            return false;
        
        // Otherwise, tally the item and signal acceptance.
        tally(+1);
        return true;
    }

    /**
     * Thread-safe means of incrementing and decrementing the pending item
     * count.
     * 
     * @param increment number by which to increment the pending item count
     */
    protected synchronized void tally(int increment) {
        tally += increment;
        lastTallyTime = System.currentTimeMillis();
    }


    /**
     * Forwards the specified item onto the downstream outlet, if one has been
     * set.
     * 
     * @param item item to be forwarded
     */
    protected void forward(P item) {
        if (downstreamOutlet != null)
            downstreamOutlet.accept(item);
    }

    /**
     * Discards the specified item onto the discard outlet, if one has been
     * set.
     * 
     * @param item item to be discarded
     */
    protected void discard(T item) {
        if (discardOutlet != null)
            discardOutlet.accept(item);
    }

    /**
     * Forwards the specified item onto the branch outlet, if one has been
     * set.
     * 
     * @param item item to be forwarded to alternate branch
     */
    protected void branch(B item) {
        if (branchOutlet != null)
            branchOutlet.accept(item);
    }

    @Override
    public synchronized boolean isEmpty() {
        return tally == 0;
    }

    @Override
    public int size() {
        return tally;
    }

    @Override
    public void start() {
        started = true;
    }

    @Override
    public void stop() {
        started = false;
    }

    @Override
    public void forceStop() {
        stop();
    }

    @Override
    public boolean isStopped() {
        return !started;
    }

    @Override
    public boolean isFinished() {
        return isStopped() && isEmpty();
    }

    @Override
    public synchronized boolean isIdle() {
        return isEmpty()
                && (System.currentTimeMillis() - lastTallyTime) > idleTimeOut;
    }

    /**
     * Set the number of milliseconds that must expire since the last time an
     * item was produced by this stage, before the stage returns to its "idle"
     * state.
     * 
     * @param timeOutMillis time-out in milliseconds
     */
    public void setIdleTimeOut(long timeOutMillis) {
        this.idleTimeOut = timeOutMillis;
    }
    
    /**
     * Get the number of milliseconds that must expire since the last time an
     * item was produced by this stage, before the stage returns to it's
     * "idle" state.
     * 
     * @return number of milliseconds before stage will go to idle state
     */
    public long getIdleTimeOut() {
        return idleTimeOut;
    }

    @Override
    public Outlet<P> getOutlet() {
        return downstreamOutlet;
    }

    @Override
    public void setOutlet(Outlet<P> outlet) {
        this.downstreamOutlet = outlet;
    }

    @Override
    public Outlet<B> getBranchOutlet() {
        return branchOutlet;
    }

    @Override
    public void setBranchOutlet(Outlet<B> outlet) {
        this.branchOutlet = outlet;
    }

    @Override
    public Outlet<T> getDiscardOutlet() {
        return discardOutlet;
    }

    @Override
    public void setDiscardOutlet(Outlet<T> discardOutlet) {
        this.discardOutlet = discardOutlet;
    }
    
    @Override
    public boolean isTerminal() {
        return downstreamOutlet == null;
    }
    
}
