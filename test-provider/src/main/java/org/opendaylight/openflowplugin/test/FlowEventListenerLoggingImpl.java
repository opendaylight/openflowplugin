/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.openflowplugin.test;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowAdded;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowRemoved;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.FlowUpdated;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.NodeExperimenterErrorNotification;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SwitchFlowRemoved;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * dummy implementation flushing events into log
 */
public class FlowEventListenerLoggingImpl implements SalFlowListener {

    private static final Logger LOG = LoggerFactory
            .getLogger(FlowEventListenerLoggingImpl.class);

    @Override
    public void onFlowAdded(FlowAdded notification) {
        LOG.info("flow to be added.........................." + notification.toString());
        LOG.info("added flow Xid........................." + notification.getTransactionId().getValue());
        LOG.info("-----------------------------------------------------------------------------------");
    }

    @Override
    public void onFlowRemoved(FlowRemoved notification) {
        LOG.debug("removed flow.........................." + notification.toString());
        LOG.debug("remove flow Xid........................." + notification.getTransactionId().getValue());
        LOG.debug("-----------------------------------------------------------------------------------");
    }

    @Override
    public void onFlowUpdated(FlowUpdated notification) {
        LOG.debug("updated flow.........................." + notification.toString());
        LOG.debug("updated flow Xid........................." + notification.getTransactionId().getValue());
        LOG.debug("-----------------------------------------------------------------------------------");
    }

    @Override
    public void onNodeErrorNotification(NodeErrorNotification notification) {
    //commenting as we have a NodeErrorListener
    /*    LOG.error("Error notification  flow Xid........................."
                + notification.getTransactionId().getValue());
        LOG.debug("notification Begin-Transaction:"
                + notification.getTransactionUri()
                + "-----------------------------------------------------------------------------------");
    */
    }

    @Override
    public void onNodeExperimenterErrorNotification(
            NodeExperimenterErrorNotification notification) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSwitchFlowRemoved(SwitchFlowRemoved notification) {
        LOG.debug("Switch flow removed : Cookies..................."
                + notification.getCookie().toString());
        LOG.debug("-----------------------------------------------------------------------------------");
    }
}
