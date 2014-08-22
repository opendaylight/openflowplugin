/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

/**
 * Simple wrapper which allows an instance of {@link ProcessFlow} to pose as a
 * full-fledged {@link ProcessStageOutlet} instance.
 * 
 * @author Thomas Vachuska
 * 
 * @param <T> type of item accepted/taken by the process as a stage
 * @param <P> type of item produced by the process as a stage
*/
public class FlowAsStage<T, P> implements ProcessStageOutlet<T, P> {

    private ProcessFlow flow;
    
    /**
     * Creates a wrapper for presenting the given process flow as a process
     * stage outlet.
     * 
     * @param flow process flow to be presented as a single process stage
     */
    public FlowAsStage(ProcessFlow flow) {
        if (flow == null)
            throw new NullPointerException("flow cannot be null");
        this.flow = flow;
    }

    @Override
    public void setDiscardOutlet(Outlet<T> discardOutlet) {
        // TODO Auto-generated method stub
    }

    @Override
    public Outlet<T> getDiscardOutlet() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setOutlet(Outlet<P> outlet) {
        // TODO Auto-generated method stub
    }

//    @SuppressWarnings("unchecked")
    @Override
    public Outlet<P> getOutlet() {
        return null;
//        Class<? extends ProcessStageOutlet<T, ?>> firstStageClass = 
//            (Class<? extends ProcessStageOutlet<T, ?>>) flow.getStageClasses().get(0);
//        return flow.getOutlet(firstStageClass);
    }

    @Override
    public boolean isTerminal() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean accept(T item) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public void start() {
        flow.start();
    }

    @Override
    public void stop() {
        flow.stop();
    }

    @Override
    public void forceStop() {
        flow.forceStop();
    }

    @Override
    public boolean isStopped() {
        return flow.isStopped();
    }

    @Override
    public boolean isFinished() {
        return flow.isFinished();
    }

    @Override
    public boolean isIdle() {
        return flow.isIdle();
    }

}
