/*
 * Copyright (c) 2017 Ericsson India Global Services Pvt Ltd. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.openflowplugin.applications.reconciliation;

import com.google.common.util.concurrent.ListenableFuture;
import org.opendaylight.openflowplugin.api.openflow.device.DeviceInfo;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.openflowplugin.rf.state.rev170713.ResultState;

/*
 * Provider to start the reconciliation of a node
 * Provider to end the reconciliation of a node
 */
public interface ReconciliationNotificationListener extends AutoCloseable {
    /*
     * This method will be a callback from RF to start the application
     * reconciliation. If the reconciliation fails for the services with a priority, the reconciliation of the node for
     * other services with lower priority has to be carried out or cancelled depending on the resultstate decided by
     * the services so that it is "fail fast" and doesn't block. This enhancement would be done later and currently the
     * reconciliation for all the services is carried out irrespective of the intent.
     *
     * @param nodeId - Node InstanceIdentifier
     *
     * @return the nodeId
     */
    ListenableFuture<Boolean> startReconciliation(DeviceInfo node);

    /*
     * This method will be a callback from RF when dpn disconnects during
     * reconciliation
     *
     * @param nodeId - Node InstanceIdentifier
     *
     * @return the nodeId
     */
    ListenableFuture<Boolean> endReconciliation(DeviceInfo node);

    /*
     * Priority of the application. The priority should be a set of values from which the user should be able to select
     * for the service instead of allowing any random values for priority from the user. Enhancement to be made to
     * restrict the user to choose from among a set of priorities by making this enum. Also enhancement to be made to
     * add the service reconciliation property as a configurable property.
     *
     * @return the priority of the service
     */
    int getPriority();

    /*
     * Name of the application
     *
     * @return the name of the service
     */
    String getName();

    /*
     * Application's intent when the application's reconciliation fails
     *
     * @return the intent of the service if the reconciliation fails
     */
    ResultState getResultState();
}
