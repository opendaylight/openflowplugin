/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of a staged process flow.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public class DefaultProcessFlow implements ProcessFlow {

    private Class<?> lastStageClass = null; // the class of the last stage in
                                            // the flow

    private Class<?> lastStageItemClass = null; // the class of the produced
                                                // item from the last stage in
                                                // the flow

    /** List of all registered stages. */
    private List<Class<? extends ProcessStageOutlet<?, ?>>> stageClasses = 
        new ArrayList<Class<? extends ProcessStageOutlet<?, ?>>>();

    /**
     * Mapping of process stage classes to the corresponding outlet instance
     * for the stage represented by that class.
     */
    private Map<Class<?>, Outlet<?>> outlets = new HashMap<Class<?>, Outlet<?>>();

    /**
     * Auxiliary wrapper to allow the process flow to be viewed as a single
     * process stage.
     */
    private FlowAsStage<?, ?> stageWrapper = null;

    
    /**
     * Get the internal set of bindings to track process stages for their
     * corresponding stage classes.
     * 
     * @return internal stage class to outlet instance bindings
     */
    protected final Map<Class<?>, Outlet<?>> getStageOutletBindings() {
        return outlets;
    }

    @Override
    public List<Class<? extends ProcessStageOutlet<?, ?>>> getStageClasses() {
        return Collections.unmodifiableList(stageClasses);
    }

    @Override
    public int getStageCount() {
        return stageClasses.size();
    }

    @Override
    public <T, P> void addStageClass(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                     Class<T> acceptedItemClass,
                                     Class<P> producedItemClass) {
        // Verify that the stage is not already part of the flow.
        if (stageClasses.contains(stageClass))
            throw new IllegalStateException("Stage '" + stageClass.getName()
                    + "' cannot be present twice in the flow.");

        // If the terminal process stage has already been added, which is
        // signified by the fact that the stage classes list not being empty
        // and the last stage item class being null, bail out now.
        if (lastStageItemClass == null && stageClasses.size() > 0)
            throw new IllegalStateException("Stage '" + stageClass.getName()
                    + "' cannot be added after a terminal stage.");

        // Insist that the accepted item class is not null.
        if (acceptedItemClass == null)
            throw new NullPointerException("acceptedItemType must not be null");

        // If the stage being added is not the first stage, verify that its
        // outlet class consumes the type of items that the prior stage
        // produces. If it does not, bail out.
        if (lastStageItemClass != null
                && !lastStageItemClass.equals(acceptedItemClass))
            throw new IllegalStateException("Stage '" + stageClass.getName()
                    + "' must consume items of type '"
                    + lastStageItemClass.getName() + "'");

        // Add the stage class to the list of classes and record the produced
        // item class as the last stage item class and the stage class
        stageClasses.add(stageClass);
        lastStageClass = stageClass;
        lastStageItemClass = producedItemClass;
    }

    /**
     * Creates and returns a ready-to-throw IllegalStageException with the
     * specified message and pertaining to the given process stage class.
     * 
     * @param stageClass process stage class
     * @param message exception message suffix
     * @return ready-to-throw {@link IllegalStateException}
     */
    protected IllegalStateException noSuchStage(Class<?> stageClass,
                                                String message) {
        return new IllegalStateException("Stage '" + stageClass.getName()
                + "' is not part of this process flow: " + message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Outlet<T> getOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass) {
        // Make sure the given class stage is supported first.
        if (!stageClasses.contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");
        // Then return the outlet mapped to the specified stage class
        return (Outlet<T>) outlets.get(stageClass);
    }

    /**
     * Returns the outlet that is downstream of the specified stage.
     * 
     * @param <P> type of item produced by the specified stage
     * @param stageClass the class of stage from which we want to go downstream
     * @return the instance of the downstream outlet; or null if none exists
     */
    @SuppressWarnings("unchecked")
    protected <P> Outlet<P> getDownstreamOutlet(Class<? extends ProcessStageOutlet<?, P>> stageClass) {
        int si = stageClasses.indexOf(stageClass);

        // Find the downstream stage class
        Class<? extends ProcessStageOutlet<P, ?>> nextStageClass = si == getStageCount() - 1 ? null
                : (Class<? extends ProcessStageOutlet<P, ?>>) stageClasses
                    .get(si + 1);

        return nextStageClass == null ? null : getOutlet(nextStageClass);
    }

    /**
     * Returns the outlet that is upstream of the specified stage.
     * 
     * @param <T> type of item produced by the upstream stage and accepted by
     *        the specified stage
     * @param stageClass the class of stage from which we want to go upstream
     * @return the instance of the upstream outlet; or null if none exists
     */
    @SuppressWarnings("unchecked")
    protected <T> ProcessStageOutlet<?, T> getUpstreamOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass) {
        int si = stageClasses.indexOf(stageClass);

        // Find the upstream stage class
        Class<? extends ProcessStageOutlet<?, T>> prevStageClass = si == 0 ? null
                : (Class<? extends ProcessStageOutlet<?, T>>) stageClasses
                    .get(si - 1);

        return prevStageClass == null ? null
                : (ProcessStageOutlet<?, T>) outlets.get(prevStageClass);
    }

    /**
     * Connect the given outlet to its outlet downstream in the process flow.
     * 
     * @param <T> type of items consumed by this stage
     * @param <P> type of items consumed by the downstream stage
     * @param stageClass class of the process stage for which the process
     *        stage instance is to be added
     * @param outlet stackable outlet to be connected to its downstream
     *        partner
     * @return true if the outlet was connected; false otherwise
     */
    protected <T, P> boolean connectToDownstreamOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                                       StackableOutlet<T, P> outlet) {
        Outlet<P> ndo = getDownstreamOutlet(stageClass);
        if (ndo == null)
            return false;
        outlet.setOutlet(ndo);
        return true;
    }

    /**
     * Connect the given outlet to its upstream partner in the process flow.
     * 
     * @param <T> type of items consumed by this stage stage
     * @param stageClass class of the process stage for which the process
     *        stage instance is to be added
     * @param outlet outlet to be connected to its upstream partner
     * @return true if the outlet was connected; false otherwise
     */
    protected <T> boolean connectToUpstreamOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass,
                                                  Outlet<T> outlet) {
        ProcessStageOutlet<?, T> puo = getUpstreamOutlet(stageClass);
        if (puo == null)
            return false;
        puo.setOutlet(outlet);
        return true;
    }

    /**
     * Register the specified stage outlet in the stageClass-to-outlet
     * bindings.
     * 
     * @param <T> type of items consumed by this stage
     * @param <P> type of items consumed by the downstream stage
     * @param stageClass stage class for which the outlet is to be registered
     * @param stageOutlet outlet to be registered
     */
    protected <T, P> void registerOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                         ProcessStageOutlet<T, P> stageOutlet) {
        outlets.put(stageClass, stageOutlet);
        // TODO : if outlets.put returns an outlet, we need to disconnect it
        // from the flow properly

    }

    /**
     * Unregister the specified stage outlet from the stageClass-to-outlet
     * bindings.
     * 
     * @param <T> type of items consumed by this stage
     * @param <P> type of items consumed by the downstream stage
     * @param stageClass stage class for which the outlet is to be
     *        unregistered
     * @param stageOutlet outlet to be unregistered
     * @return true if the outlet was unregistered; false it it has not been
     *         registered to begin with
     */
    protected <T, P> boolean unregisterOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                              ProcessStageOutlet<T, P> stageOutlet) {
        // was it in the map?
        return outlets.remove(stageClass) != null;
    }

    @Override
    public <T, P> boolean add(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                              ProcessStageOutlet<T, P> stageOutlet) {
        // Confirm that the stage class is part of the process flow.
        if (!stageClasses.contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");

        registerOutlet(stageClass, stageOutlet);

        // Connect to the downstream outlet first.
        boolean downstreamConnected = connectToDownstreamOutlet(stageClass,
                                                                stageOutlet);

        // Connect the upstream outlet to this outlet.
        boolean upstreamConnected = connectToUpstreamOutlet(stageClass,
                                                            stageOutlet);

        return upstreamConnected && downstreamConnected;
    }

    /**
     * Disconnects the given outlet from its downstream outlet.
     * 
     * @param <T> type of items consumed by this stage
     * @param <P> type of items consumed by the downstream stage
     * @param stageClass the class of the stage
     * @param stageOutlet the outlet instance
     * @return true if a disconnect occurred
     */
    protected <T, P> boolean disconnectFromDownstreamOutlet(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                                            ProcessStageOutlet<T, P> stageOutlet) {
        Outlet<P> nextOutlet = stageOutlet.getOutlet();
        stageOutlet.setOutlet(null);
        return nextOutlet != null;
    }

    /**
     * Disconnects the given outlet from its upstream outlet.
     * 
     * @param <T> type of items consumed by this stage
     * @param stageClass the class of the stage
     * @param stageOutlet the outlet instance
     * @return true if a disconnect occurred
     */
    protected <T> boolean disconnectFromUpstreamOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass,
                                                       ProcessStageOutlet<T, ?> stageOutlet) {
        ProcessStageOutlet<?, T> prevOutlet = getUpstreamOutlet(stageClass);
        if (prevOutlet == null)
            return false;

        prevOutlet.setOutlet(null);
        return true;
    }

    @Override
    public <T, P> boolean remove(final Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                 final ProcessStageOutlet<T, P> stageOutlet) {

        // Confirm that the stage class is part of the process flow.
        if (!stageClasses.contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");

        // Connect the upstream outlet to this outlet.
        disconnectFromUpstreamOutlet(stageClass, stageOutlet);

        // Connect to the downstream outlet first.
        disconnectFromDownstreamOutlet(stageClass, stageOutlet);

        return unregisterOutlet(stageClass, stageOutlet);
    }

    @Override
    public int getStageLoad(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        // Make sure the given class stage is supported first.
        if (!stageClasses.contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");
        ProcessStageOutlet<?, ?> stage = getStageOutlet(stageClass);
        return stage == null ? 0 : stage.size();
    }

    @Override
    public boolean isExecutable(final Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        // Make sure the given class stage is supported first.
        if (!stageClasses.contains(stageClass))
            throw noSuchStage(stageClass, "No such stage");
        Outlet<?> outlet = outlets.get(stageClass);
        if (outlet == null || !(outlet instanceof StackableOutlet<?, ?>))
            return false;

        return (stageClass == lastStageClass)
                || !((StackableOutlet<?, ?>) outlet).isTerminal();
    }

    @Override
    public boolean isExecutable() {
        // If any stage is not executable, the flow as a whole is not either.
        for (Class<? extends ProcessStageOutlet<?, ?>> sc : getStageClasses())
            if (!isExecutable(sc))
                return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized <T, P> ProcessStageOutlet<T, P> getAsProcessStage() {
        if (stageWrapper == null)
            stageWrapper = new FlowAsStage<T, P>(this);
        return (ProcessStageOutlet<T, P>) stageWrapper;
    }

    /**
     * Auxiliary method to view the stage outlet as a process stage outlet,
     * rather than just an outlet.
     * 
     * @param stageClass process stage class
     * @return process stage outlet servicing this process stage
     */
    private ProcessStageOutlet<?, ?> getStageOutlet(Class<? extends ProcessStageOutlet<?, ?>> stageClass) {
        return (ProcessStageOutlet<?, ?>) outlets.get(stageClass);
    }

    @Override
    public void start() {
        for (int i = stageClasses.size() - 1; i >= 0; i--)
            getStageOutlet(stageClasses.get(i)).start();
    }

    @Override
    public void stop() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : stageClasses)
            getStageOutlet(stageClass).stop();
    }

    @Override
    public void forceStop() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : stageClasses)
            getStageOutlet(stageClass).forceStop();
    }

    @Override
    public boolean isStopped() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : stageClasses)
            if (!getStageOutlet(stageClass).isStopped())
                return false;
        return true;
    }

    @Override
    public boolean isFinished() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : stageClasses)
            if (!getStageOutlet(stageClass).isFinished())
                return false;
        return true;
    }

    @Override
    public boolean isIdle() {
        for (Class<? extends ProcessStageOutlet<?, ?>> stageClass : stageClasses)
            if (!getStageOutlet(stageClass).isIdle())
                return false;
        return true;
    }

}
