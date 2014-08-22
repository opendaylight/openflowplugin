/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendaylight.util.ThroughputTracker;

/**
 * Basic implementation of a process flow with stages interconnected
 * indirectly via fan-out outlets at the top of each stage.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultFanoutProcessFlow extends DefaultProcessFlow implements
        FanoutProcessFlow {

    /**
     * Internal projection of the stage outlet onto the fanout outlet which it
     * really is.
     * 
     * @param <T> type of the items consumed by this process stage
     * @param stageClass class of the process stage
     * @return fan-out outlet associated with the given process stage
     */
    protected <T> MeteringFanoutOutlet<T> getFanout(Class<? extends ProcessStageOutlet<T, ?>> stageClass) {
        return (MeteringFanoutOutlet<T>) getOutlet(stageClass);
    }

    @Override
    public <T, P> void addStageClass(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                     Class<T> acceptedItemClass,
                                     Class<P> producedItemClass) {
        super.addStageClass(stageClass, acceptedItemClass, producedItemClass);
        getStageOutletBindings().put(stageClass, new MeteringFanoutOutlet<T>());
    }

    @Override
    public <T> Set<Outlet<T>> getOutlets(Class<? extends ProcessStageOutlet<T, ?>> stageClass) {
        return Collections.unmodifiableSet(getFanout(stageClass).getOutlets());
    }

    @Override
    public boolean isExecutable(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        // Fetch the aggregating outlet for the specified stage class and
        // return false if there is no such stage class or if the aggregating
        // outlet does not have any outlets registered.
        MeteringFanoutOutlet<?> fo = (MeteringFanoutOutlet<?>) getStageOutletBindings()
            .get(stageClass);
        if (fo == null)
            throw noSuchStage(stageClass, "No such stage");
        return fo.getOutlets().size() > 0;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This specialization ignores the registration request as the fanout
     * already serves as the stage outlet.
     */
    @Override
    protected <T, P> void registerOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                         ProcessStageOutlet<T, P> stageOutlet) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * This specialization ignores the unregistration request as the fanout
     * needs to remain.
     */
    @Override
    protected <T, P> boolean unregisterOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                              ProcessStageOutlet<T, P> stageOutlet) {
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This specialization connects the incoming outlet to its fan-out.
     */
    @Override
    protected <T> boolean connectToUpstreamOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass,
                                                  Outlet<T> outlet) {
        MeteringFanoutOutlet<T> fo = getFanout(stageClass);
        return fo.add(outlet);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This specialization disconnects the departing outlet from its fan-out.
     */
    @Override
    protected <T> boolean disconnectFromUpstreamOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass,
                                                       ProcessStageOutlet<T, ?> stageOutlet) {
        MeteringFanoutOutlet<T> fo = getFanout(stageClass);
        return fo.remove(stageOutlet);
    }

    /**
     * Gets the set of 'process' stage outlets belonging to the fan-out of the
     * given stage.
     * <p>
     * Consumers should abstain from controlling these individual process
     * stages, unless they have 'ownership'.
     * 
     * @param stageClass process stage class
     * @return set of process stage outlets servicing this process stage
     */
    public Set<ProcessStageOutlet<?, ?>> getStageOutlets(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        MeteringFanoutOutlet<?> fanout = (MeteringFanoutOutlet<?>) 
                getStageOutletBindings().get(stageClass);
        Set<ProcessStageOutlet<?, ?>> psos = new HashSet<ProcessStageOutlet<?, ?>>(fanout.size());

        // Copy the set into one of 'process' stage outlet types.
        for (Outlet<?> outlet : fanout.getOutlets())
            psos.add((ProcessStageOutlet<?, ?>) outlet);

        return psos;
    }

    @Override
    public int getStageLoad(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        // Make sure the given class stage is supported first.
        if (!getStageClasses().contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");
        int load = 0;
        for (ProcessStageOutlet<?, ?> so : getStageOutlets(stageClass))
            load += so.size();
        return load;
    }

    @Override
    public ThroughputTracker getStageTracker(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        // Make sure the given class stage is supported first.
        if (!getStageClasses().contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");
        MeteringFanoutOutlet<?> fanout = (MeteringFanoutOutlet<?>) 
                    getStageOutletBindings().get(stageClass);
        return fanout.getTracker();
    }


    @Override
    public void start() {
        List<Class<? extends ProcessStageOutlet<?, ?>>> stageClasses = getStageClasses();
        for (int i = stageClasses.size() - 1; i >= 0; i--) {
            getStageTracker(stageClasses.get(i)).reset();
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClasses.get(i)))
                pso.start();
        }
    }

    @Override
    public void stop() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : getStageClasses()) {
            getStageTracker(stageClass).freeze();
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClass))
                pso.stop();
        }
    }

    @Override
    public void forceStop() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : getStageClasses()) {
            getStageTracker(stageClass).freeze();
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClass))
                pso.forceStop();
        }
    }

    @Override
    public boolean isStopped() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : getStageClasses())
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClass))
                if (!pso.isStopped())
                    return false;
        return true;
    }

    @Override
    public boolean isFinished() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : getStageClasses())
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClass))
                if (!pso.isFinished())
                    return false;
        return true;
    }

    @Override
    public boolean isIdle() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : getStageClasses())
            for (ProcessStageOutlet<?, ?> pso : getStageOutlets(stageClass))
                if (!pso.isIdle())
                    return false;
        return true;
    }

}
