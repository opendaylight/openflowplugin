/*
 * (c) Copyright 2010 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.util.stage;

import java.util.List;

/**
 * Abstraction of a sequence of interconnected {@link ProcessStage process
 * stages}, which form a process flow, and which as a whole can be viewed as a
 * single process stage.
 * <p>
 * Note that the flow may contain loops and that input may be provided
 * directly to any of the stages comprising the overall process flow.
 * 
 * @author Thomas Vachuska
 * @author Simon Hunt
 */
public interface ProcessFlow extends ProcessStage {

    /**
     * Get an ordered list of classes for the process stage outlets that
     * comprise the flow.
     * 
     * @return ordered collection of process stage outlet classes
     */
    public List<Class<? extends ProcessStageOutlet<?, ?>>> getStageClasses();
    
    /**
     * Get the number of process stage classes currently in the process flow.
     * 
     * @return number of process stage classes in the flow
     */
    public int getStageCount();
    
    /**
     * Adds the specified stage class to the end of the process flow.
     * 
     * @param <T> type of the items accepted by the new stage
     * @param <P> type of the items produced by the new stage
     * @param stageClass process stage class
     * @param acceptedItemClass class of the items accepted by the new stage
     * @param producedItemClass class of the items produced by the new stage;
     *        if null the stage being added will be considered a terminal
     *        stage, which means no other stages can be added to the process
     *        flow
     */
    public <T, P> void addStageClass(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                     Class<T> acceptedItemClass, Class<P> producedItemClass);

    /**
     * Gets the outlet that currently services the specified class of the
     * process stage.
     * 
     * @param <T> type of item accepted by the specified process stage
     * @param stageClass class of the process stage outlet for which the
     *        process stage instance is to be returned
     * @return outlet instance currently servicing the given process flow
     *         stage; null of no process stage outlet instance has been added
     *         to the flow yet
     * @throws IllegalStateException if there is no stage corresponding to the
     *         given class
     */
    public <T> Outlet<T> getOutlet(Class<? extends ProcessStageOutlet<T, ?>> stageClass);

    /**
     * Adds an process stage to the process flow. The outlet is added at the
     * stage inferred by the class of the outlet.
     * 
     * @param <T> type of items accepted by this stage
     * @param <P> type of items produced by this stage
     * @param stageClass class of the process stage for which the 
     *        process stage instance is to be added
     * @param stageOutlet process stage outlet to be inserted into the flow
     * @return true if the process stage is not already part of the flow;
     *         false otherwise
     * @throws IllegalStateException if there is no stage corresponding to the
     *         given class of the outlet
     */
    public <T, P> boolean add(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                              ProcessStageOutlet<T, P> stageOutlet);
    
    /**
     * Removes a process stage outlet from the process flow.
     * 
     * @param <T> type of items accepted by this stage
     * @param <P> type of items produced by this stage
     * @param stageClass class of the process stage for which the
     *        process stage instance is to be removed
     * @param stageOutlet process stage outlet to be removed from the flow
     * @return true if the process stage was part of the flow; false otherwise
     * @throws IllegalStateException if there is no stage corresponding to the
     *         given class of the outlet
     */
    public <T, P> boolean remove(Class<? extends ProcessStageOutlet<T, P>> stageClass,
                                 ProcessStageOutlet<T, P> stageOutlet);

    /**
     * Indicates whether the process flow as a whole is executable, which
     * means that each of its stages has a process stage outlet registered
     * with it.
     * <p>
     * The implementation is expected to be a logical AND of
     * {@link #isExecutable(Class)} for all the stages of the process flow as
     * returned by {@link #getStageClasses()}.
     * 
     * @return true if the process flow can be started; false otherwise
     */
    public boolean isExecutable();
    
    /**
     * Indicates whether the given process flow stage has a process stage
     * outlet registered and therefore whether it is executable or not.
     * 
     * @param stageClass class of the process stage outlet to be validated
     * @return true if there is an outlet currently servicing the given
     *         process flow stage; false otherwise
     */
    public boolean isExecutable(Class<? extends ProcessStageOutlet<?, ?>> stageClass);

    /**
     * Get the number of items currently pending or being processed by the
     * specified stage.
     * 
     * @param stageClass class of the process stage outlet whose load is
     *        requested
     * @return number of items accepted, but not yet finished processing
     *         through the specified stage
     */
    public int getStageLoad(Class<? extends ProcessStageOutlet<?, ?>> stageClass);

    /**
     * Projects this process flow as a single stage.
     *
     * @param <T> type of items accepted by the first internal stage
     * @param <P> type of items produced by the last internal stage
     * @return this flow cast as a process stage outlet
     * @throws UnsupportedOperationException if projection not supported
     */
    public <T, P> ProcessStageOutlet<T, P> getAsProcessStage();

}
