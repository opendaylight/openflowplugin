/*
 * (c) Copyright 2013 Hewlett-Packard Development Company, L.P.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.of.controller.pipeline;

import org.opendaylight.of.lib.dt.DataPathId;
import org.opendaylight.of.lib.dt.TableId;
import org.opendaylight.of.lib.mp.MBodyTableFeatures;
import org.opendaylight.of.lib.msg.OfmFlowMod;
import org.opendaylight.util.api.NotFoundException;

import java.util.Set;

/** Provides basic pipeline understanding for the controller.
 * <p>
 * The implementing class will:
 * <ul>
 *     <li>
 *         build and maintain a pipeline definition of all datapaths
 *     </li>
 *     <li>
 *         return a list of suitable tables for a given <em>Flow Mod</em>
 *     </li>
 * </ul>
 * 
 * <p>
 * A Pipeline definition is built by way of collecting {@link MBodyTableFeatures}
 * when a call <em>getDefintition()</em> is invoked for the first time
 * on a given data path. The definition is then cached in the Pipeline Manager 
 * and will be available for any subsequent invocations of 
 * <em>getDefintition()</em>
 * <p>
 * It creating the pipeline definition from OpenFlow constructs.
 * <p>
 * It is also assumed that <em>Flow Advisor</em> will facilitate the deep vendor 
 * nuances on the valid set of matches at every table.
 *
 * @author Pramod Shanbhag
 * @author Radhika Hegde
 */
public interface PipelineReader {

    /** Returns the set of table IDs of the given datapath's pipeline definition
     * that support the given flow mod.
     * <p>
     * PipelineReader makes this decision based on the available pipeline 
     * definition. Within a pipeline definition, each of the tables will have 
     * their corresponding capabilities in terms of matches, actions and 
     * configurations that they support.
     *    
     * @param flowmod the <em>FlowMod</em> message
     * @param dpid the datapath to which the <em>FlowMod</em> message
     *        is to be sent
     * @return the list of supported table IDs matching the given flow mod
     * @throws NullPointerException if either parameter is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    Set<TableId> align(OfmFlowMod flowmod, DataPathId dpid);

    /** Returns the pipeline definition for the given datapath. 
     * A Pipeline definition is built by way of collecting 
     * {@link MBodyTableFeatures} when a call <em>getDefintition()</em> is 
     * invoked for the first time on a given data path. The definition is then 
     * cached in the Pipeline Manager and will be available for any subsequent 
     * invocations of <em>getDefintition()</em>
     * 
     * @param dpid the datapath to which the given <em>FlowMod</em> message
     *        is to be sent
     * @return the pipeline definition for the given datapath
     * @throws NullPointerException if dpid is null
     * @throws NotFoundException if the specified datapath does not exist
     */
    PipelineDefinition getDefinition(DataPathId dpid);
}
